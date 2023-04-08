/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.textmc;

import calebxzhou.rdi.mixin.AccessNativeImage;
import com.google.gson.*;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import icyllis.modernui.ModernUI;
import icyllis.modernui.graphics.font.*;
import icyllis.modernui.text.*;
import icyllis.modernui.textmc.mixin.MixinClientLanguage;
import icyllis.modernui.view.View;
import icyllis.modernui.view.ViewConfiguration;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;
import org.lwjgl.system.MemoryUtil;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static icyllis.modernui.ModernUI.*;

/**
 * Modern Text Engine for Minecraft. Layout text component and extract style info and generate
 * or cache render information for Minecraft text system.
 * <p>
 * This is a streaming layout engine which differs from Modern UI core.
 *
 * @since 2.0
 */
public class TextLayoutEngine {

    /**
     * Instance on main/render thread
     */
    private static volatile TextLayoutEngine sInstance;


    /**
     * Config values
     */
    //public static int sDefaultFontSize;
    public static volatile boolean sFixedResolution = false;
    public static volatile boolean sSuperSampling = false;
    public static volatile int sTextDirection = View.TEXT_DIRECTION_FIRST_STRONG;
    /**
     * Time in seconds to recycle a render node in the cache.
     */
    public static volatile int sCacheLifespan = 12;
    public static volatile int sRehashThreshold = 100;

    /**
     * Matches Slack emoji shortcode.
     */
    public static final Pattern EMOJI_SHORTCODE_PATTERN =
            Pattern.compile("(\\:(\\w|\\+|\\-)+\\:)(?=|[\\!\\.\\?]|$)");

    private static final ChatFormatting[] FORMATTING_TABLE = ChatFormatting.values();

    /*
     * Draw and cache all glyphs of all fonts needed
     * Lazy-loading because we are waiting for render system to initialize
     */
    //private GlyphManagerForge glyphManager;

    /*
     * A cache of recently seen strings to their fully laid-out state, complete with color changes and texture
     * coordinates of
     * all pre-rendered glyph images needed to display this string. The weakRefCache holds strong references to the Key
     * objects used in this map.
     */
    //private WeakHashMap<Key, Entry> stringCache = new WeakHashMap<>();

    /*
     * Every String passed to the public renderString() function is added to this WeakHashMap. As long as As long as
     * Minecraft
     * continues to hold a strong reference to the String object (i.e. from TileEntitySign and ChatLine) passed here,
     *  the
     * weakRefCache map will continue to hold a strong reference to the Key object that said strings all map to
     * (multiple strings
     * in weakRefCache can map to a single Key if those strings only differ by their ASCII digits).
     */
    //private WeakHashMap<String, Key> weakRefCache = new WeakHashMap<>();

    /*private final Cache<VanillaTextKey, TextRenderNode> stringCache = Caffeine.newBuilder()
            .expireAfterAccess(20, TimeUnit.SECONDS)
            .build();*/

    // init first
    private final GlyphManager mGlyphManager = GlyphManager.getInstance();

    /**
     * Temporary Key object re-used for lookups with stringCache.get(). Using a temporary object like this avoids the
     * overhead of allocating new objects in the critical rendering path. Of course, new Key objects are always created
     * when adding a mapping to stringCache.
     */
    private final VanillaTextKey mVanillaLookupKey = new VanillaTextKey();
    private Map<VanillaTextKey, TextRenderNode> mVanillaCache = new HashMap<>();

    /**
     * For styled texts.
     */
    private Map<MutableComponent, TextRenderNode> mComponentCache = new HashMap<>();

    /**
     * For deeply-processed texts.
     */
    private final MultilayerTextKey.Lookup mMultilayerLookupKey = new MultilayerTextKey.Lookup();
    private Map<MultilayerTextKey, TextRenderNode> mMultilayerCache = new HashMap<>();

    /**
     * Shared layout engine.
     */
    private final TextLayoutProcessor mProcessor = new TextLayoutProcessor(this);

    /**
     * For fast digit replacement and obfuscated char rendering.
     * Map from 'derived font' to 'ASCII 33(!) to 126(~) characters with their standard advances and relative advances'.
     */
    private final Map<Font, FastCharSet> mFastCharMap = new HashMap<>();
    private final Function<Font, FastCharSet> mFastCharFunc = new Function<>() {
        @Nonnull
        @Override
        public FastCharSet apply(Font font) {
            // initial table
            GLBakedGlyph[] glyphs = new GLBakedGlyph[94]; // 126 - 33 + 1
            // normalized offsets
            float[] offsets = new float[glyphs.length];
            char[] chars = new char[1];
            int n = 0;
            // 48 to 57, always cache all digits
            for (int i = 0; i < 10; i++) {
                chars[0] = (char) ('0' + i);
                // no shaping
                GlyphVector vector = mGlyphManager.createGlyphVector(font, chars);
                float advance = (float) vector.getGlyphPosition(1).getX();
                GLBakedGlyph glyph = mGlyphManager.lookupGlyph(font, vector.getGlyphCode(0));
                Objects.requireNonNull(glyph, font + " does not support ASCII digits");
                glyphs[i] = glyph;
                // '0' is standard, because it's wider than other digits in general
                if (i == 0) {
                    // 0 is standard advance
                    offsets[n] = advance;
                } else {
                    // relative offset to standard advance, to center the glyph
                    offsets[n] = (offsets[0] - advance) / 2f;
                }
                n++;
            }
            // 33 to 47
            for (int i = 0; i < 15; i++) {
                chars[0] = (char) (33 + i);
                // no shaping
                GlyphVector vector = mGlyphManager.createGlyphVector(font, chars);
                float advance = (float) vector.getGlyphPosition(1).getX();
                // too wide
                if (advance + 0.5f > offsets[0]) {
                    continue;
                }
                GLBakedGlyph glyph = mGlyphManager.lookupGlyph(font, vector.getGlyphCode(0));
                if (glyph != null) {
                    glyphs[n] = glyph;
                    offsets[n] = (offsets[0] - advance) / 2f;
                    n++;
                }
            }
            // 58 to 126
            for (int i = 0; i < 69; i++) {
                chars[0] = (char) (58 + i);
                // no shaping
                GlyphVector vector = mGlyphManager.createGlyphVector(font, chars);
                float advance = (float) vector.getGlyphPosition(1).getX();
                // too wide
                if (advance + 0.5f > offsets[0]) {
                    continue;
                }
                GLBakedGlyph glyph = mGlyphManager.lookupGlyph(font, vector.getGlyphCode(0));
                if (glyph != null) {
                    glyphs[n] = glyph;
                    offsets[n] = (offsets[0] - advance) / 2f;
                    n++;
                }
            }
            if (n < glyphs.length) {
                glyphs = Arrays.copyOf(glyphs, n);
                offsets = Arrays.copyOf(offsets, n);
            }
            float level = getResolutionLevel();
            // the cache will be reset when resolution level changed
            for (int i = 0; i < n; i++) {
                offsets[i] /= level;
            }
            return new FastCharSet(glyphs, offsets);
        }
    };

    /**
     * Gui scale = 4.
     */
    public static final int EMOJI_SCALE = 4;
    public static final int EMOJI_BASE_SIZE = 9;
    public static final int EMOJI_SIZE = EMOJI_BASE_SIZE * EMOJI_SCALE;

    /**
     * Emoji sequence to sprite index (used as glyph code in emoji atlas).
     */
    private final HashMap<CharSequence, EmojiEntry> mEmojiMap = new HashMap<>();
    /**
     * The key used to lookup Emoji IDs.
     */
    private final CharSequenceBuilder mEmojiLookupKey = new CharSequenceBuilder();
    /**
     * Shortcodes to Emoji char sequences.
     */
    private final HashMap<String, String> mEmojiShortcodes = new HashMap<>();
    /**
     * The emoji texture atlas.
     */
    private GLFontAtlas mEmojiAtlas;
    private ByteBuffer mEmojiBuffer;
    private boolean mEmojiScanned = false;

    /**
     * @param id       used as glyph code
     * @param location resource location
     * @param sequence emoji char sequence
     */
    private record EmojiEntry(int id, ResourceLocation location, String sequence) {
    }

    private final Predicate<TextRenderNode> mTicker = node -> node.tick(sCacheLifespan);

    /**
     * Determine font size. Integer.
     */
    private float mCoordinateScale;
    /**
     * Determine font size. Integer.
     */
    private float mResolutionLevel;
    /**
     * Text direction.
     */
    private TextDirectionHeuristic mTextDirectionHeuristic = TextDirectionHeuristics.FIRSTSTRONG_LTR;

    /*
     * Remove all formatting code even though it's invalid {@link #getFormattingByCode(char)} == null
     */
    //private static final Pattern FORMATTING_REMOVE_PATTERN = Pattern.compile("\u00a7.");

    /*
     * True if digitGlyphs[] has been assigned and cacheString() can begin replacing all digits with '0' in the string.
     */
    //private boolean digitGlyphsReady = false;

    private int mTimer;

    private TextLayoutEngine() {
        /* StringCache is created by the main game thread; remember it for later thread safety checks */
        //mainThread = Thread.currentThread();

        /* Pre-cache the ASCII digits to allow for fast glyph substitution */
        //cacheDigitGlyphs();

        // init
        reload();
        // events
       // MinecraftForge.EVENT_BUS.register(this);
        // OpenGL texture ID changed
        mGlyphManager.addAtlasResizeCallback(TextRenderType::clear);
    }

    /**
     * Get the global instance.
     *
     * @return the instance
     */
    @Nonnull
    public static TextLayoutEngine getInstance() {
        if (sInstance == null) {
            synchronized (TextLayoutEngine.class) {
                if (sInstance == null) {
                    sInstance = new TextLayoutEngine();
                }
            }
        }
        return sInstance;
    }

    /**
     * @return the glyph manager
     */
    @Nonnull
    public GlyphManager getGlyphManager() {
        return mGlyphManager;
    }

    /**
     * Cleanup layout cache.
     */
    public void cleanup() {
        int size = getCacheCount();
        mVanillaCache.clear();
        mComponentCache.clear();
        mMultilayerCache.clear();
        mFastCharMap.clear();
        boolean rehash = size >= sRehashThreshold;
        if (rehash) {
            // Create new HashMap so that the internal hashtable of old maps are released as well
            mVanillaCache = new HashMap<>();
            mComponentCache = new HashMap<>();
            mMultilayerCache = new HashMap<>();
            //mDigitMap = new HashMap<>();
        }
        // Clear TextRenderType instances, but font textures are not released
        TextRenderType.clear();
        if (size > 0) {
            LOGGER.info(MARKER, "Cleanup {} text layout entries, rehash: {}", size, rehash);
        }
    }

    /**
     * Reload layout engine.
     * Called when resolution level or language changed. This will call {@link #cleanup()}.
     */
    public void reload() {
        cleanup();

        final int scale = Math.round(ViewConfiguration.get().getViewScale() * 2);
        final float oldLevel = mResolutionLevel;
        if (sFixedResolution) {
            // make font size to 16 (8 * 2)
            mResolutionLevel = 2;
        } else {
            // Note max font size is 96, see FontPaint, font size will be (8 * resolution) in Minecraft
            if (!sSuperSampling || !GLFontAtlas.sLinearSampling) {
                mResolutionLevel = Math.min(scale, 9);
            } else if (scale > 2) {
                // super sampling, give it a bit larger, so looks smoother
                mResolutionLevel = Math.min((int) Math.ceil(scale * 4 / 3f), 12);
            } else {
                // 1 or 2
                mResolutionLevel = scale;
            }
        }
		//mResolutionLevel=24;
        mCoordinateScale = scale;

        Locale locale = ModernUI.getSelectedLocale();
        boolean layoutRtl = TextUtils.getLayoutDirectionFromLocale(locale) == View.LAYOUT_DIRECTION_RTL;
        mTextDirectionHeuristic = switch (sTextDirection) {
            case View.TEXT_DIRECTION_ANY_RTL -> TextDirectionHeuristics.ANYRTL_LTR;
            case View.TEXT_DIRECTION_LTR -> TextDirectionHeuristics.LTR;
            case View.TEXT_DIRECTION_RTL -> TextDirectionHeuristics.RTL;
            case View.TEXT_DIRECTION_LOCALE -> TextDirectionHeuristics.LOCALE;
            case View.TEXT_DIRECTION_FIRST_STRONG_LTR -> TextDirectionHeuristics.FIRSTSTRONG_LTR;
            case View.TEXT_DIRECTION_FIRST_STRONG_RTL -> TextDirectionHeuristics.FIRSTSTRONG_RTL;
            default -> layoutRtl ? TextDirectionHeuristics.FIRSTSTRONG_RTL :
                    TextDirectionHeuristics.FIRSTSTRONG_LTR;
        };

        if (oldLevel == 0) {
            LOGGER.info(MARKER, "Loaded text layout engine, resolution level: {}, locale: {}, layout RTL: {}",
                    mResolutionLevel, locale, layoutRtl);
        } else {
            LOGGER.info(MARKER, "Reloaded text layout engine, resolution level: {} to {}, locale: {}, layout RTL: {}",
                    oldLevel, mResolutionLevel, locale, layoutRtl);
        }
    }

    /**
     * Called when language changed.
     */
    public void reloadResources() {
        if (!mEmojiScanned) {
            scanEmoji();
            scanShortcodes();
            mEmojiScanned = true;
        }
        reload();
    }

    private void scanEmoji() {
        final boolean emojiOnly = !ModernUITextMC.CONFIG.mBitmapReplacement ;
        Predicate<ResourceLocation> filter =
                location -> location.getPath().length() <= 64 && location.getPath().endsWith(".png");
        Minecraft.getInstance().getResourceManager().listResources("emoji", filter)
                .forEach((location, resource) -> {
                    String[] paths = location.getPath().split("/");
                    if (paths.length == 0) {
                        return;
                    }
                    String name = paths[paths.length - 1];
                    String[] parts = name.substring(0, name.length() - 4).split("-");
                    if (parts.length == 0) {
                        return;
                    }
                    int[] codePoints = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) {
                        try {
                            int codePoint = Integer.parseInt(parts[i], 16);
                            if (!Character.isValidCodePoint(codePoint)) {
                                return;
                            }
                            if (i == 0 && emojiOnly && !Emoji.isEmoji(codePoint)) {
                                return;
                            }
                            codePoints[i] = codePoint;
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    final String sequence = new String(codePoints, 0, codePoints.length);
                    mEmojiMap.computeIfAbsent(sequence,
                            __ -> new EmojiEntry(mEmojiMap.size(), location, sequence));
                });
        LOGGER.info("Scanned emoji map size: {}", mEmojiMap.size());
    }

    private void scanShortcodes() {
        int mismatched = 0;
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(
                new ResourceLocation(ID, "emoji_data.json"));
             BufferedReader reader = new BufferedReader(new InputStreamReader(
                     inputStream, StandardCharsets.UTF_8))) {
            JsonObject object = GsonHelper.fromJson(new Gson(), reader, JsonObject.class);
            if (object != null) {
                for (var entry : object.entrySet()) {
                    JsonArray shortcodes = entry.getValue().getAsJsonArray().get(3).getAsJsonArray();
                    String[] parts = entry.getKey().split("-");
                    if (parts.length == 0) {
                        return;
                    }
                    mEmojiLookupKey.clear();
                    for (String part : parts) {
                        try {
                            int codePoint = Integer.parseInt(part, 16);
                            mEmojiLookupKey.addCodePoint(codePoint);
                        } catch (NumberFormatException e) {
                            return;
                        }
                    }
                    final String sequence;
                    EmojiEntry cachedEntry = mEmojiMap.get(mEmojiLookupKey);
                    // try to reuse emoji sequence
                    if (cachedEntry != null) {
                        sequence = cachedEntry.sequence;
                    } else {
                        // try with variation selector-16 removed
                        mEmojiLookupKey.clear();
                        for (String part : parts) {
                            try {
                                int codePoint = Integer.parseInt(part, 16);
                                if (codePoint == 0xfe0f) {
                                    continue;
                                }
                                mEmojiLookupKey.addCodePoint(codePoint);
                            } catch (NumberFormatException e) {
                                return;
                            }
                        }
                        cachedEntry = mEmojiMap.get(mEmojiLookupKey);
                        if (cachedEntry != null) {
                            sequence = cachedEntry.sequence;
                        } else {
                            sequence = null;
                        }
                    }
                    if (sequence != null) {
                        shortcodes.forEach(e -> mEmojiShortcodes.putIfAbsent(e.getAsString(), sequence));
                    } else {
                        mismatched++;
                    }
                }
            } else {
                LOGGER.info(MARKER, "Failed to parse emoji data");
            }
        } catch (Exception e) {
            LOGGER.info(MARKER, "Failed to load emoji data", e);
        }
        LOGGER.info("Loaded emoji shortcodes: {}, mismatched emoji sequences: {}",
                mEmojiShortcodes.size(), mismatched);
    }

    /**
     * Reload both render engine and layout engine.
     */
    public void reloadAll() {
        mGlyphManager.reload();
        LOGGER.info(MARKER, "Reloaded glyph manager");
        if (mEmojiAtlas != null) {
            mEmojiAtlas.close();
            mEmojiAtlas = null;
            LOGGER.info(MARKER, "Reloaded emoji atlas");
        }
        LayoutCache.clear();
        reload();
    }

    /**
     * Lookup cached render node for vanilla text or create the layout.
     *
     * @param text input text
     * @return the full layout
     */
    @Nonnull
    public TextRenderNode lookupVanillaNode(@Nonnull String text) {
        if (text.isEmpty()) {
            return TextRenderNode.EMPTY;
        }
        if (!RenderSystem.isOnRenderThread()) {
            // block
            return Minecraft.getInstance()
                    .submit(() -> lookupVanillaNode(text))
                    .join();
        }
        TextRenderNode node = mVanillaCache.get(mVanillaLookupKey.update(text, Style.EMPTY));
        if (node == null) {
            node = mProcessor.performVanillaLayout(text, Style.EMPTY);
            mVanillaCache.put(mVanillaLookupKey.copy(), node);
            return node;
        }
        return node.get();
    }

    /**
     * Lookup cached render node for vanilla text or create the layout.
     *
     * @param text  input text
     * @param style base style
     * @return the full layout
     */
    @Nonnull
    public TextRenderNode lookupVanillaNode(@Nonnull String text, @Nonnull Style style) {
        if (text.isEmpty()) {
            return TextRenderNode.EMPTY;
        }
        if (!RenderSystem.isOnRenderThread()) {
            // block
            return Minecraft.getInstance()
                    .submit(() -> lookupVanillaNode(text, style))
                    .join();
        }
        TextRenderNode node = mVanillaCache.get(mVanillaLookupKey.update(text, style));
        if (node == null) {
            node = mProcessor.performVanillaLayout(text, style);
            mVanillaCache.put(mVanillaLookupKey.copy(), node);
            return node;
        }
        return node.get();
    }

    /**
     * Lookup cached render node for multilayer text or create the layout.
     * To perform bidi analysis, we must have the full text of all layers.
     *
     * @param text root node
     * @return the full layout
     * @see FormattedTextWrapper
     */
    public TextRenderNode lookupComplexNode(@Nonnull FormattedText text) {
        if (text == CommonComponents.EMPTY || text == FormattedText.EMPTY) {
            return TextRenderNode.EMPTY;
        }
        if (!RenderSystem.isOnRenderThread()) {
            // block
            return Minecraft.getInstance()
                    .submit(() -> lookupComplexNode(text))
                    .join();
        }
        TextRenderNode node;
        if (text instanceof MutableComponent component) {
            node = mComponentCache.get(component);
            if (node == null) {
                node = mProcessor.performComplexLayout(text, Style.EMPTY);
                mComponentCache.put(component, node);
                return node;
            }
        } else {
            node = mMultilayerCache.get(mMultilayerLookupKey.update(text, Style.EMPTY));
            if (node == null) {
                node = mProcessor.performComplexLayout(text, Style.EMPTY);
                mMultilayerCache.put(mMultilayerLookupKey.copy(), node);
                return node;
            }
        }
        return node.get();
    }

    /**
     * Lookup cached render node for multilayer text or create the layout.
     * To perform bidi analysis, we must have the full text of all layers.
     *
     * @param text  root node
     * @param style base style
     * @return the full layout
     * @see FormattedTextWrapper
     */
    public TextRenderNode lookupComplexNode(@Nonnull FormattedText text, @Nonnull Style style) {
        if (text == CommonComponents.EMPTY || text == FormattedText.EMPTY) {
            return TextRenderNode.EMPTY;
        }
        if (!RenderSystem.isOnRenderThread()) {
            // block
            return Minecraft.getInstance()
                    .submit(() -> lookupComplexNode(text, style))
                    .join();
        }
        TextRenderNode node;
        if (text instanceof MutableComponent component) {
            node = mComponentCache.get(component);
            if (node == null) {
                node = mProcessor.performComplexLayout(text, style);
                mComponentCache.put(component, node);
                return node;
            }
        } else {
            node = mMultilayerCache.get(mMultilayerLookupKey.update(text, style));
            if (node == null) {
                node = mProcessor.performComplexLayout(text, style);
                mMultilayerCache.put(mMultilayerLookupKey.copy(), node);
                return node;
            }
        }
        return node.get();
    }

    /**
     * Lookup cached render node for multilayer text or create the layout.
     * To perform bidi analysis, we must have the full text of all layers.
     * Modern UI removed vanilla's BiDi reordering.
     * <p>
     * This method should only be used when FormattedText cannot be obtained.
     *
     * @param sequence deeply-processed sequence
     * @return the full layout
     * @see FormattedTextWrapper
     */
    @Nonnull
    public TextRenderNode lookupSequenceNode(@Nonnull FormattedCharSequence sequence) {
        if (sequence == FormattedCharSequence.EMPTY) {
            return TextRenderNode.EMPTY;
        }
        if (!RenderSystem.isOnRenderThread()) {
            // block
            return Minecraft.getInstance()
                    .submit(() -> lookupSequenceNode(sequence))
                    .join();
        }
        // check if we intercepted it by Language.getVisualOrder()
        if (sequence instanceof FormattedTextWrapper) {
            FormattedText text = ((FormattedTextWrapper) sequence).mText;
            if (text == CommonComponents.EMPTY || text == FormattedText.EMPTY) {
                return TextRenderNode.EMPTY;
            }
            TextRenderNode node;
            if (text instanceof MutableComponent component) {
                node = mComponentCache.get(component);
                if (node == null) {
                    node = mProcessor.performComplexLayout(text, Style.EMPTY);
                    mComponentCache.put(component, node);
                    return node;
                }
            } else {
                node = mMultilayerCache.get(mMultilayerLookupKey.update(text, Style.EMPTY));
                if (node == null) {
                    node = mProcessor.performComplexLayout(text, Style.EMPTY);
                    mMultilayerCache.put(mMultilayerLookupKey.copy(), node);
                    return node;
                }
            }
            return node.get();
        } else {
            TextRenderNode node = mMultilayerCache.get(mMultilayerLookupKey.update(sequence));
            if (node == null) {
                node = mProcessor.performSequenceLayout(sequence);
                mMultilayerCache.put(mMultilayerLookupKey.copy(), node);
                return node;
            }
            return node.get();
        }
    }

    /**
     * Minecraft gives us a deeply processed sequence, so we have to make the
     * it not a reordered text, see {@link MixinClientLanguage}.
     * So actually it's a copy of original text, then we can use our layout engine later
     *
     * @param sequence a char sequence copied from the original string
     * @param consumer what to do with a part of styled char sequence
     * @return {@code false} if action stopped on the way, {@code true} if the whole text was handled
     */
    /*@Deprecated
    public boolean handleSequence(FormattedCharSequence sequence, ReorderTextHandler.IConsumer consumer) {
        throw new UnsupportedOperationException();
    }*/

    /**
     * Given a grapheme cluster, locate the color emoji's pre-rendered image in the emoji atlas and
     * return its cache entry. The entry stores the texture with the pre-rendered emoji image,
     * as well as the position and size of that image within the texture.
     *
     * @param text  the text buffer
     * @param start the cluster start index (inclusive)
     * @param end   the cluster end index (exclusive)
     * @return the cached emoji sprite or null
     */
    @Nullable
    public GLBakedGlyph lookupEmoji(@Nonnull char[] text, int start, int end) {
        final EmojiEntry entry = mEmojiMap.get(mEmojiLookupKey.updateCharArray(text, start, end));
        if (entry == null) {
            return null;
        }
        if (mEmojiAtlas == null) {
            mEmojiAtlas = new GLFontAtlas(true);
            int s = (EMOJI_SIZE + GlyphManager.GLYPH_BORDER * 2);
            // RGBA, 4 bytes per pixel
            mEmojiBuffer = MemoryUtil.memCalloc(1, s * s * 4);
        }
        GLBakedGlyph glyph = mEmojiAtlas.getGlyph(entry.id);
        if (glyph != null && glyph.texture == 0) {
            return cacheEmoji(entry.id, entry.location, mEmojiAtlas, glyph);
        }
        return glyph;
    }

    /**
     * Lookup Emoji char sequence from shortcode.
     *
     * @param shortcode the shortcode, e.g. cheese
     * @return compiled Emoji code points
     */
    @Nullable
    public String lookupEmojiShortcode(@Nonnull String shortcode) {
        return mEmojiShortcodes.get(shortcode);
    }

    public void dumpEmojiAtlas() {
        if (mEmojiAtlas != null) {
            String basePath =
                    icyllis.modernui.core.NativeImage.saveDialogGet(icyllis.modernui.core.NativeImage.SaveFormat.PNG,
                            "EmojiAtlas");
            mEmojiAtlas.debug(basePath);
        }
    }

    public int getEmojiAtlasMemorySize() {
        if (mEmojiAtlas != null) {
            return mEmojiAtlas.getMemorySize();
        }
        return 0;
    }

    @Nullable
    private GLBakedGlyph cacheEmoji(int id, @Nonnull ResourceLocation location,
                                    @Nonnull GLFontAtlas atlas, @Nonnull GLBakedGlyph glyph) {
        try (InputStream inputStream = Minecraft.getInstance().getResourceManager().open(location);
             NativeImage image = NativeImage.read(inputStream)) {
            if ((image.getWidth() == EMOJI_SIZE && image.getHeight() == EMOJI_SIZE) ||
                    (image.getWidth() == EMOJI_SIZE * 2 && image.getHeight() == EMOJI_SIZE * 2)) {
                long dst = MemoryUtil.memAddress(mEmojiBuffer);
                {
                    NativeImage downSample = null;
                    if (image.getWidth() == EMOJI_SIZE * 2) {
                        // Down-sampling
                        downSample = MipmapGenerator.generateMipLevels(image, 1)[1];
                    }
                    long src;
					if(downSample!=null){
						src = ((AccessNativeImage)(Object)downSample).getPixels();
					}else{
						src = ((AccessNativeImage)(Object)image).getPixels();
					}

					// = UIManager.IMAGE_PIXELS.getLong(downSample != null ? downSample : image);
                    // Add 1 pixel transparent border to prevent texture bleeding
                    // RGBA is 4 bytes per pixel
                    long dstOff = (EMOJI_SIZE + GlyphManager.GLYPH_BORDER * 2 + GlyphManager.GLYPH_BORDER) * 4;
                    for (int i = 0; i < EMOJI_SIZE; i++) {
                        MemoryUtil.memCopy(src + (i * EMOJI_SIZE * 4), dst + dstOff, EMOJI_SIZE * 4);
                        dstOff += (EMOJI_SIZE + GlyphManager.GLYPH_BORDER * 2) * 4;
                    }
                    if (downSample != null) {
                        downSample.close();
                    }
                }
                glyph.x = 0;
                glyph.y = 0; // x and y baseline is hardcoded in TextRenderNode
                glyph.width = EMOJI_SIZE;
                glyph.height = EMOJI_SIZE;
                atlas.stitch(glyph, dst);
                return glyph;
            } else {
                atlas.setNull(id);
                LOGGER.warn(MARKER, "Emoji is not {}x or {}x, setting empty: {}", EMOJI_SIZE, EMOJI_SIZE * 2, location);
                return null;
            }
        } catch (Exception e) {
            atlas.setNull(id);
            LOGGER.warn(MARKER, "Failed to load emoji, setting empty: {}", location, e);
            return null;
        }
    }

    /**
     * Ticks the caches and clear unused entries.
     */
    static {
		ClientTickEvents.END.register(TextLayoutEngine.getInstance()::tick);
	}
	void tick(Minecraft minecraft) {
            if (mTimer == 0) {
                int oldCount = getCacheCount();
                mVanillaCache.values().removeIf(mTicker);
                mComponentCache.values().removeIf(mTicker);
                mMultilayerCache.values().removeIf(mTicker);
                if (oldCount >= sRehashThreshold) {
                    int newCount = getCacheCount();
                    if (newCount < sRehashThreshold) {
                        mVanillaCache = new HashMap<>(mVanillaCache);
                        mComponentCache = new HashMap<>(mComponentCache);
                        mMultilayerCache = new HashMap<>(mMultilayerCache);
                    }
                }
            }
            // convert ticks to seconds
            mTimer = (mTimer + 1) % 20;
    }

    /**
     * @return the number of layout entries
     */
    public int getCacheCount() {
        return mVanillaCache.size() + mComponentCache.size() + mMultilayerCache.size();
    }

    /**
     * @return measurable cache size in bytes
     */
    public int getCacheMemorySize() {
        int size = 0;
        for (var n : mVanillaCache.values()) {
            size += n.getMemorySize();
        }
        for (var n : mComponentCache.values()) {
            size += n.getMemorySize();
        }
        for (var e : mMultilayerCache.entrySet()) {
            size += e.getKey().getMemorySize();
            size += e.getValue().getMemorySize();
        }
        return size;
    }

    /**
     * Get ChatFormatting from the formatting code, but faster than Minecraft vanilla.
     *
     * @param code c
     * @return chat formatting, {@code null} if nothing
     * @see ChatFormatting#getByCode(char)
     */
    @Nullable
    public static ChatFormatting getFormattingByCode(char code) {
        code = Character.toLowerCase(code);
        for (ChatFormatting f : FORMATTING_TABLE)
            if (f.getChar() == code)
                return f;
        return null;
    }

    /**
     * Returns current GUI scale for texts.
     *
     * @return scale factor, should be an integer that converted to float
     */
    public float getCoordinateScale() {
        return mCoordinateScale;
    }

    /**
     * Returns current resolution level for texts.
     *
     * @return resolution level, should be an integer that converted to float
     */
    public float getResolutionLevel() {
        return mResolutionLevel;
    }

    /**
     * Returns current text direction algorithm.
     *
     * @return text dir
     */
    @Nonnull
    public TextDirectionHeuristic getTextDirectionHeuristic() {
        return mTextDirectionHeuristic;
    }

    /**
     * Lookup fast char glyph with given font.
     * The pair right is the offsetX to standard '0' advance alignment (already scaled by GUI factor).
     * Because we assume FAST digit glyphs are monospaced, no matter whether it's a monospaced font.
     *
     * @param font derived font including style and font size
     * @return array of all fast char glyphs 0-9 (in that order), and others
     */
    @Nonnull
    public FastCharSet lookupFastChars(@Nonnull Font font) {
        return mFastCharMap.computeIfAbsent(font, mFastCharFunc);
    }

    /**
     * FastCharSet have uniform advances. Offset[0] is the advance for all glyphs.
     * Other offsets is the relative offset to center the glyph. Normalized to
     * Minecraft GUI system.
     * <p>
     * This is used to render fast digits and obfuscated chars.
     */
    public static class FastCharSet extends GLBakedGlyph {

        public final GLBakedGlyph[] glyphs;
        public final float[] offsets;

        public FastCharSet(GLBakedGlyph[] glyphs, float[] offsets) {
            this.glyphs = glyphs;
            this.offsets = offsets;
        }
    }

    /**
     * Pre-cache the ASCII digits to allow for fast glyph substitution. Called once from the constructor and called any
     * time the font selection
     * changes at runtime via setDefaultFont().
     * <p>
     * Pre-cached glyphs for the ASCII digits 0-9 (in that order). Used by renderString() to substitute digit glyphs on
     * the fly
     * as a performance boost. The speed up is most noticeable on the F3 screen which rapidly displays lots of changing
     * numbers.
     * The 4 element array is index by the font style (combination of Font.PLAIN, Font.BOLD, and Font.ITALIC), and each
     * of the
     * nested elements is index by the digit value 0-9.
     *
     * @deprecated {@link GlyphManagerForge#lookupDigits(Font)}
     */
    @Deprecated
    private void cacheDigitGlyphs() {
        /* Need to cache each font style combination; the digitGlyphsReady = false disabled the normal glyph
        substitution mechanism */
        //digitGlyphsReady = false;
        /*digitGlyphs[FormattingCode.PLAIN] = getOrCacheString("0123456789").glyphs;
        digitGlyphs[FormattingCode.BOLD] = getOrCacheString("\u00a7l0123456789").glyphs;
        digitGlyphs[FormattingCode.ITALIC] = getOrCacheString("\u00a7o0123456789").glyphs;
        digitGlyphs[FormattingCode.BOLD | FormattingCode.ITALIC] = getOrCacheString("\u00a7l\u00a7o0123456789")
        .glyphs;*/
        //digitGlyphsReady = true;
    }

    @Nullable
    @Deprecated
    private TextRenderNode generateAndCache(VanillaTextKey key, @Nonnull CharSequence string,
                                            @Nonnull final Style style) {
        /*final int length = string.length();
        final TextProcessRegister register = this.register;

        register.beginProcess(style);

        int codePoint;
        TexturedGlyph glyph;
        for (int stringIndex = 0, glyphIndex = 0; stringIndex < length; stringIndex++) {
            char c1 = string.charAt(stringIndex);

            if (stringIndex + 1 < length) {
                if (c1 == '\u00a7') {
                    TextFormatting formatting = fromFormattingCode(string.charAt(++stringIndex));
                    if (formatting != null) {
                        register.applyFormatting(formatting, glyphIndex);
                    }*/
        /*switch (code) {
         *//* Obfuscated *//*
                        case 16:
                            if (state.setObfuscated(true)) {
                                if (!state.isDigitMode()) {
                                    state.setDigitGlyphs(glyphManager.lookupDigits(state.getFontStyle(),
                                    sDefaultFontSize));
                                }
                                if (state.isDigitMode() && state.hasDigit()) {
                                    strings.add(StringRenderInfo.ofDigit(state.getDigitGlyphs(),
                                            state.getColor(), state.toDigitIndexArray()));
                                } else if (!glyphs.isEmpty()) {
                                    strings.add(StringRenderInfo.ofText(glyphs.toArray(new TexturedGlyph[0]),
                                            state.getColor()));
                                }
                            }
                            break;

                        *//* Bold *//*
                        case 17:
                            if (state.setBold(true)) {
                                if (state.getObfuscatedCount() > 0) {
                                    strings.add(StringRenderInfo.ofObfuscated(state.getDigitGlyphs(),
                                            state.getColor(), state.getObfuscatedCount()));
                                } else if (state.isDigitMode() && state.hasDigit()) {
                                    strings.add(StringRenderInfo.ofDigit(state.getDigitGlyphs(),
                                            state.getColor(), state.toDigitIndexArray()));
                                }
                                if (state.isDigitMode() || state.isObfuscated()) {
                                    state.setDigitGlyphs(glyphManager.lookupDigits(state.getFontStyle(),
                                    sDefaultFontSize));
                                }
                            }
                            break;

                        case 18:
                            state.setStrikethrough(true);
                            break;

                        case 19:
                            state.setUnderline(true);
                            break;

                        case 20:
                            if (state.setItalic(true)) {
                                if (state.getObfuscatedCount() > 0) {
                                    strings.add(StringRenderInfo.ofObfuscated(state.getDigitGlyphs(),
                                            state.getColor(), state.getObfuscatedCount()));
                                } else if (state.isDigitMode() && state.hasDigit()) {
                                    strings.add(StringRenderInfo.ofDigit(state.getDigitGlyphs(),
                                            state.getColor(), state.toDigitIndexArray()));
                                }
                                if (state.isDigitMode() || state.isObfuscated()) {
                                    state.setDigitGlyphs(glyphManager.lookupDigits(state.getFontStyle(),
                                    sDefaultFontSize));
                                }
                            }
                            break;

                        *//* Reset *//*
                        case 21: {
                            int pColor = state.getColor();
                            if (state.setDefaultColor()) {
                                if (state.getObfuscatedCount() > 0) {
                                    strings.add(StringRenderInfo.ofObfuscated(state.getDigitGlyphs(),
                                            pColor, state.getObfuscatedCount()));
                                } else if (state.isDigitMode() && state.hasDigit()) {
                                    strings.add(StringRenderInfo.ofDigit(state.getDigitGlyphs(),
                                            pColor, state.toDigitIndexArray()));
                                } else if (!glyphs.isEmpty()) {
                                    strings.add(StringRenderInfo.ofText(glyphs.toArray(new TexturedGlyph[0]),
                                            pColor));
                                }
                                if (state.isUnderline()) {
                                    effects.add(EffectRenderInfo.ofUnderline(state.getUnderlineStart(), state
                                    .getAdvance(), pColor));
                                }
                                if (state.isStrikethrough()) {
                                    effects.add(EffectRenderInfo.ofStrikethrough(state.getUnderlineStart(), state
                                    .getAdvance(), pColor));
                                }
                                state.setDefaultFontStyle();
                                state.setDefaultObfuscated();
                                state.setDefaultStrikethrough();
                                state.setDefaultUnderline();
                            } else {
                                if (state.isObfuscated() && state.setDefaultObfuscated() && state.getObfuscatedCount
                                () > 0) {
                                    strings.add(StringRenderInfo.ofObfuscated(state.getDigitGlyphs(),
                                            pColor, state.getObfuscatedCount()));
                                }
                            }
                        }
                            *//*fontStyle = defStyle;
                            color = defColor;
                        {
                            boolean p = strikethrough;
                            strikethrough = defStrikethrough;
                            if (!strikethrough && p) {
                                effects.add(EffectRenderInfo.ofStrikethrough(strikethroughX, advance, color));
                            }
                        }
                        {
                            boolean p = underline;
                            underline = defUnderline;
                            if (!underline && p) {
                                effects.add(EffectRenderInfo.ofUnderline(underlineX, advance, color));
                            }
                        }
                        {
                            boolean p = obfuscated;
                            obfuscated = defObfuscated;
                            if (!obfuscated && p) {
                                if (!glyphs.isEmpty()) {
                                    strings.add(new StringRenderInfo(glyphs.toArray(new TexturedGlyph[0]), color,
                                    true));
                                    glyphs.clear();
                                }
                            }
                        }*//*
                        break;

                        default:
                            if (code != -1) {
                                int c = Color3i.fromFormattingCode(code).getColor();
                                //processState.setColor(c, this::addStringAndEffectInfo);
                                *//*if (color != c) {
                                    if (!glyphs.isEmpty()) {
                                        strings.add(new StringRenderInfo(glyphs.toArray(new TexturedGlyph[0]), color,
                                         obfuscated));
                                        color = c;
                                        glyphs.clear();
                                    }
                                    if (strikethrough) {
                                        effects.add(new EffectRenderInfo(strikethroughX, advance, color,
                                        EffectRenderInfo.STRIKETHROUGH));
                                        strikethroughX = advance;
                                    }
                                    if (underline) {
                                        effects.add(new EffectRenderInfo(underlineX, advance, color, EffectRenderInfo
                                        .UNDERLINE));
                                        underlineX = advance;
                                    }
                                }*//*
                            }
                            break;
                    }*/
                    /*continue;

                } else if (Character.isHighSurrogate(c1)) {
                    char c2 = string.charAt(stringIndex + 1);
                    if (Character.isLowSurrogate(c2)) {
                        codePoint = Character.toCodePoint(c1, c2);
                        ++stringIndex;
                    } else {
                        codePoint = c1;
                    }
                } else {
                    codePoint = c1;
                }
            } else {
                codePoint = c1;
            }*/

            /*if (codePoint >= 48 && codePoint <= 57) {
                TexturedGlyph[] digits = glyphManager.lookupDigits(processRegister.getFontStyle(), sDefaultFontSize);
                //processState.nowDigit(i, digits, this::addStringInfo);
                processRegister.addAdvance(digits[0].advance);
            } else {
                //processState.nowText(this::addStringInfo);
                glyph = glyphManager.lookupGlyph(codePoint, processRegister.getFontStyle(), sDefaultFontSize);
                processRegister.addAdvance(glyph.advance);
                glyphs.add(glyph);
            }*/

            /*if (register.peekFontStyle()) {
                register.setDigitGlyphs(glyphManager.lookupDigits(
                        register.getFontStyle(), sDefaultFontSize));
            }*/
            /*if (register.isObfuscated() || (codePoint <= 57 && codePoint >= 48)) {
                register.depositDigit(stringIndex, glyphManager.lookupDigits(register.getFontStyle(),
                sDefaultFontSize));
            } else {
                register.depositGlyph(glyphManager.lookupGlyph(codePoint, register.getFontStyle(), sDefaultFontSize));
            }
            glyphIndex++;
        }*/

        /*if (strikethrough) {
            effects.add(new EffectRenderInfo(strikethroughX, advance, color, EffectRenderInfo.STRIKETHROUGH));
        }

        if (underline) {
            effects.add(new EffectRenderInfo(underlineX, advance, color, EffectRenderInfo.UNDERLINE));
        }

        if (!glyphs.isEmpty()) {
            strings.add(new StringRenderInfo(glyphs.toArray(new TexturedGlyph[0]), color, obfuscated));
            glyphs.clear();
        }*/
        //addStringAndEffectInfo(processState);

        //register.finishProcess();

        // Async work, waiting for render thread
        if (!RenderSystem.isOnRenderThread()) {
            // The game thread is equal to render thread now
            /*synchronized (lock) {
                Minecraft.getInstance()
                        .submit(() -> generateVanillaNode(key, string, style))
                        .whenComplete((n, t) -> {
                            atomicNode.set(n);
                            synchronized (lock) {
                                lock.notify();
                            }
                        });
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                TextRenderNode node = atomicNode.get();
                atomicNode.set(null);
                return node;
            }*/
            return Minecraft.getInstance()
                    .submit(() -> generateAndCache(key, string, style))
                    .join();
        }

        /*final TextLayoutProcessor data = this.mProcessor;

         *//* Step 1 *//*
        char[] text = resolveFormattingCodes(data, string, style);

        final TextRenderNode node;

        if (text.length > 0) {
            *//* Step 2-5 *//*
            startBidiAnalysis(data, text);

            if (data.mAllList.isEmpty()) {
                *//* Sometimes naive, too young too simple *//*
                node = TextRenderNode.EMPTY;
            } else {
                *//* Step 6 *//*
                adjustGlyphIndex(data);

                *//* Step 7 *//*
                insertColorState(data);

                *//* Step 8 *//*
                GlyphRender[] glyphs = data.wrapGlyphs();

                *//* Step 9 *//*
                node = new TextRenderNode(glyphs, data.mAdvance, data.mHasEffect);
            }
        }

        *//* Sometimes naive, too young too simple *//*
        else {
            node = TextRenderNode.EMPTY;
        }
        data.release();

        stringCache.put(key, node);*/

        return null;
    }

    /**
     * Add a string to the string cache by perform full layout on it, remembering its glyph positions, and making sure
     * that
     * every font glyph used by the string is pre-rendering. If this string has already been cached, then simply return
     * its
     * existing Entry from the cache. Note that for caching purposes, this method considers two strings to be identical
     * if they
     * only differ in their ASCII digits; the renderString() method performs fast glyph substitution based on the actual
     * digits
     * in the string at the time.
     *
     * @param str this String will be laid out and added to the cache (or looked up, if already cached)
     * @return the string's cache entry containing all the glyph positions
     */
    @Nonnull
    @Deprecated
    private Entry getOrCacheString(@Nonnull String str) {
        /*
         * New Key object allocated only if the string was not found in the StringCache using lookupKey. This
         * variable must
         * be outside the (entry == null) code block to have a temporary strong reference between the time when the
         * Key is
         * added to stringCache and added to weakRefCache.
         */
        Key key;

        /* Either a newly created Entry object for the string, or the cached Entry if the string is already in the
        cache */
        Entry entry;

        /* Don't perform a cache lookup from other threads because the stringCache is not synchronized */
        RenderSystem.assertOnRenderThread();
        //if () {
        /* Re-use existing lookupKey to avoid allocation overhead on the critical rendering path */
        //lookupKey.str = str;

        /* If this string is already in the cache, simply return the cached Entry object */
        //entry = stringCache.getIfPresent(lookupKey);
        //}
        //ModernUI.LOGGER.info("cache size {}", stringCache.size());
        /* If string is not cached (or not on main thread) then layout the string */
        //if (false) {
        //ModernUI.LOGGER.info("new entry for {}", str);
        /* layoutGlyphVector() requires a char[] so create it here and pass it around to avoid duplication later on */
        char[] text = str.toCharArray();

        /* First extract all formatting codes from the string */
        entry = new Entry();
        int length = extractFormattingCodes(entry, str, text); // return total string length except formatting codes

        /* Layout the entire string, splitting it up by formatting codes and the Unicode bidirectional algorithm */
        List<Glyph> glyphList = new ArrayList<>();

        entry.advance = layoutBidiString(glyphList, text, 0, length, entry.codes);

        /* Convert the accumulated Glyph list to an array for efficient storage */
        entry.glyphs = new Glyph[glyphList.size()];
        entry.glyphs = glyphList.toArray(entry.glyphs);

        /*
         * Sort Glyph array by stringIndex so it can be compared during rendering to the already sorted ColorCode array.
         * This will apply color codes in the string's logical character order and not the visual order on screen.
         */
        Arrays.sort(entry.glyphs);

        /* Do some post-processing on each Glyph object */
        int colorIndex = 0, shift = 0;
        for (int glyphIndex = 0; glyphIndex < entry.glyphs.length; glyphIndex++) {
            Glyph glyph = entry.glyphs[glyphIndex];

            /*
             * Adjust the string index for each glyph to point into the original string with unstripped color codes.
             * The while
             * loop is necessary to handle multiple consecutive color codes with no visible glyphs between them.
             * These new adjusted
             * stringIndex can now be compared against the color stringIndex during rendering. It also allows lookups
             *  of ASCII
             * digits in the original string for fast glyph replacement during rendering.
             */
            //while (colorIndex < entry.codes.length && glyph.stringIndex + shift >= entry.codes[colorIndex]
            // .stringIndex) {
            //    shift += 2;
            //    colorIndex++;
            //}
            //glyph.stringIndex += shift;
        }

        /*
         * Do not actually cache the string when called from other threads because GlyphCache.cacheGlyphs() will not
         * have been called
         * and the cache entry does not contain any texture data needed for rendering.
         */
        //if (mainThread == Thread.currentThread()) {
        /* Wrap the string in a Key object (to change how ASCII digits are compared) and cache it along with the
        newly generated Entry */
        key = new Key();

        /* Make a copy of the original String to avoid creating a strong reference to it */
        key.str = str;
        //entry.keyRef = new WeakReference<>(key);
        //stringCache.put(key, entry);
        //ModernUI.LOGGER.debug("cache string {}", key.str);
        //}
        //lookupKey.str = null;
        //}

        /* Do not access weakRefCache from other threads since it is unsynchronized, and for a newly created entry,
        the keyRef is null */
        /*if (mainThread == Thread.currentThread()) {
         *//*
         * Add the String passed into this method to the stringWeakMap so it keeps the Key reference live as long as
         the String is in use.
         * If an existing Entry was already found in the stringCache, it's possible that its Key has already been
         garbage collected. The
         * code below checks for this to avoid adding (str, null) entries into weakRefCache. Note that if a new Key
         object was created, it
         * will still be live because of the strong reference created by the "key" variable.
         *//*
            Key oldKey = entry.keyRef.get();
            if (oldKey != null) {
                //weakRefCache.put(str, oldKey);
            }
            lookupKey.str = null;
        }*/

        /* Return either the existing or the newly created entry so it can be accessed immediately */
        return entry;
    }

    /**
     * Finally, we got a piece of text with same layout direction, font style and whether to be obfuscated.
     *
     * @param data   an object to store the results
     * @param text   the plain text (without formatting codes) to analyze
     * @param start  start index (inclusive) of the text
     * @param limit  end index (exclusive) of the text
     * @param flag   layout direction, either {@link Font#LAYOUT_LEFT_TO_RIGHT} or {@link Font#LAYOUT_RIGHT_TO_LEFT}
     * @param font   the derived font with fontStyle and fontSize
     * @param random whether to layout obfuscated characters or not
     * @param effect text render effect
     */
    @Deprecated
    private void layoutFont(TextLayoutProcessor data, char[] text, int start, int limit, int flag, Font font,
                            boolean random,
                            byte effect) {
        /*if (random) {
         *//* Random is not worthy to layout *//*
            layoutRandom(data, text, start, limit, flag, font, effect);
        } else {
            *//* The glyphCode matched to the same codePoint is specified in the font, they are different in different
            font *//*
            GlyphVector vector = glyphManager.layoutGlyphVector(font, text, start, limit, flag);
            int num = vector.getNumGlyphs();

            final GlyphManagerForge.VanillaGlyph[] digits = glyphManager.lookupDigits(font);
            final float factor = glyphManager.getResolutionFactor();

            for (int i = 0; i < num; i++) {
                *//* Back compatibility for Java 8, since LayoutGlyphVector should not have non-standard glyphs
         * HarfBuzz is introduced in Java 11 or higher
         *//*
         *//*if (vector.getGlyphMetrics(i).getAdvanceX() == 0 &&
                        vector.getGlyphMetrics(i).getBounds2D().getWidth() == 0) {
                    continue;
                }*//*

                int stripIndex = vector.getGlyphCharIndex(i) + start;
                Point2D point = vector.getGlyphPosition(i);

                float offset = (float) (point.getX() / factor);

                *//*if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
                    offset += data.mLayoutRight;
                } else {
                    offset += data.mAdvance;
                }*//*

                char o = text[stripIndex];
                *//* Digits are not on SMP *//*
                if (o == '0') {
                    //data.mStyleList.add(new DigitGlyphRender(digits, effect, stripIndex, offset));
                    continue;
                }

                int glyphCode = vector.getGlyphCode(i);
                GlyphManagerForge.VanillaGlyph glyph = glyphManager.lookupGlyph(font, glyphCode);

                //data.mStyleList.add(new StandardGlyphRender(glyph, effect, stripIndex, offset));
            }

            float totalAdvance = (float) (vector.getGlyphPosition(num).getX() / factor);
            *//*data.mAdvance += totalAdvance;

            if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
                data.finishStyleRun(-totalAdvance);
                data.mLayoutRight -= totalAdvance;
            } else {
                data.finishStyleRun(0);
            }*//*
        }*/
    }

    /*private void layoutEmoji(TextProcessData data, int codePoint, int start, int flag) {
        float offset;
        if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
            offset = data.layoutRight;
        } else {
            offset = data.advance;
        }

        data.minimalList.add(new StandardGlyphRender(glyphManager.lookupEmoji(codePoint), TextRenderEffect.NO_EFFECT,
         start, offset));

        offset += 12;

        data.advance += offset;

        if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
            data.finishFontLayout(-offset);
            data.layoutRight -= offset;
        } else {
            data.finishFontLayout(0);
        }
    }*/

    /**
     * Simple layout for random digits
     *
     * @param data   an object to store the results
     * @param text   the plain text (without formatting codes) to analyze
     * @param start  start index (inclusive) of the text
     * @param limit  end index (exclusive) of the text
     * @param flag   layout direction, either {@link Font#LAYOUT_LEFT_TO_RIGHT} or {@link Font#LAYOUT_RIGHT_TO_LEFT}
     * @param font   the derived font with fontStyle and fontSize
     * @param effect text render effect
     */
   /* @Deprecated
    private void layoutRandom(TextLayoutProcessor data, char[] text, int start, int limit, int flag, Font font,
                              byte effect) {
        final GlyphManagerForge.VanillaGlyph[] digits = glyphManager.lookupDigits(font);
        final float stdAdv = digits[0].getAdvance();

        float offset;
        if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
            offset = data.mLayoutRight;
        } else {
            offset = data.mAdvance;
        }

        *//* Process code point *//*
        for (int i = start; i < limit; i++) {
            data.mStyleList.add(new RandomGlyphRender(digits, effect, start + i, offset));

            offset += stdAdv;

            char c1 = text[i];
            if (i + 1 < limit && Character.isHighSurrogate(c1)) {
                char c2 = text[i + 1];
                if (Character.isLowSurrogate(c2)) {
                    ++i;
                }
            }
        }

        data.mAdvance += offset;

        if (flag == Font.LAYOUT_RIGHT_TO_LEFT) {
            data.finishStyleRun(-offset);
            data.mLayoutRight -= offset;
        } else {
            data.finishStyleRun(0);
        }
    }*/

    @Deprecated
    private void insertColorState(@Nonnull TextLayoutProcessor data) {
        /* Sometimes naive */
        /*else {
            if (underline) {
                if (strikethrough) {
                    glyphs.forEach(e -> e.effect = TextRenderEffect.UNDERLINE_STRIKETHROUGH);
                } else {
                    glyphs.forEach(e -> e.effect = TextRenderEffect.UNDERLINE);
                }
                data.hasEffect = true;
            } else if (strikethrough) {
                glyphs.forEach(e -> e.effect = TextRenderEffect.STRIKETHROUGH);
                data.hasEffect = true;
            }
        }*/

        /*float start1 = 0;
        float start2 = 0;

        int glyphIndex = 0;
        for (int codeIndex = 1; codeIndex < data.codes.size(); codeIndex++) {
            FormattingStyle code = data.codes.get(codeIndex);

            while (glyphIndex < data.allList.size() - 1 &&
                    (pg = data.allList.get(glyphIndex)).stringIndex < code.stringIndex) {
                //data.advance += pg.getAdvance();
                glyphIndex++;
            }

            if (color != code.getColor()) {
                colors.add(new ColorStateInfo(glyphIndex, color = code.getColor()));

                boolean b = code.isUnderline();

                if (underline) {
                    //effects.add(EffectRenderInfo.underline(start1, data.advance, color));
                }
                if (b) {
                    start1 = data.advance;
                }
                underline = b;

                b = code.isStrikethrough();
                if (strikethrough) {
                    //effects.add(EffectRenderInfo.strikethrough(start2, data.advance, color));
                }
                if (b) {
                    start2 = data.advance;
                }
                strikethrough = b;

            } else {
                boolean b = code.isUnderline();

                if (underline != b) {
                    if (!b) {
                        //effects.add(EffectRenderInfo.underline(start1, data.advance, color));
                    } else {
                        start1 = data.advance;
                    }
                    underline = b;
                }

                b = code.isStrikethrough();
                if (strikethrough != b) {
                    if (!b) {
                        //effects.add(EffectRenderInfo.strikethrough(start2, data.advance, color));
                    } else {
                        start2 = data.advance;
                    }
                    strikethrough = b;
                }
            }
        }

        while (glyphIndex < data.allList.size()) {
            //data.advance += data.list.get(glyphIndex).getAdvance();
            glyphIndex++;
        }*/

        /*if (underline) {
            //effects.add(EffectRenderInfo.underline(start1, data.advance, color));
            data.mergeUnderline(color);
        }
        if (strikethrough) {
            //effects.add(EffectRenderInfo.strikethrough(start2, data.advance, color));
            data.mergeStrikethrough(color);
        }*/
    }

    /**
     * Remove all color codes from the string by shifting data in the text[] array over so it overwrites them. The value
     * of each
     * color code and its position (relative to the new stripped text[]) is also recorded in a separate array. The color
     * codes must
     * be removed for a font's context sensitive glyph substitution to work (like Arabic letter middle form).
     *
     * @param cacheEntry each color change in the string will add a new ColorCode object to this list
     * @param str        the string from which color codes will be stripped
     * @param text       on input it should be an identical copy of str; on output it will be string with all color
     *                   codes removed
     * @return the length of the new stripped string in text[]; actual text.length will not change because the array is
     * not reallocated
     */
    @Deprecated
    private int extractFormattingCodes(Entry cacheEntry, @Nonnull String str, char[] text) {
        List<FormattingCode> codeList = new ArrayList<>();
        int start = 0, shift = 0, next;

        byte fontStyle = Font.PLAIN;
        byte renderStyle = 0;
        byte colorCode = -1;

        /* Search for section mark characters indicating the start of a color code (but only if followed by at least
        one character) */
        while ((next = str.indexOf('\u00A7', start)) != -1 && next + 1 < str.length()) {
            /*
             * Remove the two char color code from text[] by shifting the remaining data in the array over on top of it.
             * The "start" and "next" variables all contain offsets into the original unmodified "str" string. The
             * "shift"
             * variable keeps track of how many characters have been stripped so far, and it's used to compute
             * offsets into
             * the text[] array based on the start/next offsets in the original string.
             */
            System.arraycopy(text, next - shift + 2, text, next - shift, text.length - next - 2);

            /* Decode escape code used in the string and change current font style / color based on it */
            int code = "0123456789abcdefklmnor".indexOf(Character.toLowerCase(str.charAt(next + 1)));
            switch (code) {
                /* Random style */
                case 16:
                    break;

                /* Bold style */
                case 17:
                    fontStyle |= 1;
                    break;

                /* Strikethrough style */
                case 18:
                    renderStyle |= FormattingCode.STRIKETHROUGH;
                    cacheEntry.needExtraRender = true;
                    break;

                /* Underline style */
                case 19:
                    renderStyle |= FormattingCode.UNDERLINE;
                    cacheEntry.needExtraRender = true;
                    break;

                /* Italic style */
                case 20:
                    fontStyle |= 2;
                    break;

                /* Reset style */
                case 21:
                    fontStyle = 0;
                    renderStyle = 0;
                    colorCode = -1; // we need to back default color
                    break;

                /* Otherwise, must be a color code or some other unsupported code */
                default:
                    if (code >= 0) {
                        colorCode = (byte) code;
                        //fontStyle = Font.PLAIN; // This may be a bug in Minecraft's original FontRenderer
                        //renderStyle = 0; // This may be a bug in Minecraft's original FontRenderer
                    }
                    break;
            }

            /* Create a new ColorCode object that tracks the position of the code in the original string */
            FormattingCode formatting = new FormattingCode();
            formatting.stringIndex = next;
            formatting.stripIndex = next - shift;
            formatting.color = Color3i.fromFormattingCode(colorCode);
            formatting.fontStyle = fontStyle;
            formatting.renderEffect = renderStyle;
            codeList.add(formatting);

            /* Resume search for section marks after skipping this one */
            start = next + 2;
            shift += 2;
        }

        /* Convert the accumulated ColorCode list to an array for efficient storage */
        //cacheEntry.codes = new ColorCode[codeList.size()];
        cacheEntry.codes = codeList.toArray(new FormattingCode[0]);

        /* Return the new length of the string after all color codes were removed */
        /* This should be equal to current text char[] length */
        return text.length - shift;
    }

    /**
     * Split a string into contiguous LTR or RTL sections by applying the Unicode Bidirectional Algorithm. Calls
     * layoutString()
     * for each contiguous run to perform further analysis.
     *
     * @param glyphList will hold all new Glyph objects allocated by layoutFont()
     * @param text      the string to lay out
     * @param start     the offset into text at which to start the layout
     * @param limit     the (offset + length) at which to stop performing the layout
     * @return the total advance (horizontal distance) of this string
     */
    @Deprecated
    private float layoutBidiString(List<Glyph> glyphList, char[] text, int start, int limit, FormattingCode[] codes) {
        float advance = 0;

        /* Avoid performing full bidirectional analysis if text has no "strong" right-to-left characters */
        if (Bidi.requiresBidi(text, start, limit)) {
            /* Note that while requiresBidi() uses start/limit the Bidi constructor uses start/length */
            Bidi bidi = new Bidi(text, start, null, 0, limit - start, Bidi.DIRECTION_DEFAULT_LEFT_TO_RIGHT);

            /* If text is entirely right-to-left, then insert an EntryText node for the entire string */
            if (bidi.isRightToLeft()) {
                return layoutStyle(glyphList, text, start, limit, Font.LAYOUT_RIGHT_TO_LEFT, advance, codes);
            }

            /* Otherwise text has a mixture of LTR and RLT, and it requires full bidirectional analysis */
            else {
                int runCount = bidi.getRunCount();
                byte[] levels = new byte[runCount];
                Integer[] ranges = new Integer[runCount];

                /* Reorder contiguous runs of text into their display order from left to right */
                for (int index = 0; index < runCount; index++) {
                    levels[index] = (byte) bidi.getRunLevel(index);
                    ranges[index] = index;
                }
                Bidi.reorderVisually(levels, 0, ranges, 0, runCount);

                /*
                 * Every GlyphVector must be created on a contiguous run of left-to-right or right-to-left text. Keep
                 *  track of
                 * the horizontal advance between each run of text, so that the glyphs in each run can be assigned a
                 * position relative
                 * to the start of the entire string and not just relative to that run.
                 */
                for (int visualIndex = 0; visualIndex < runCount; visualIndex++) {
                    int logicalIndex = ranges[visualIndex];

                    /* An odd numbered level indicates right-to-left ordering */
                    int layoutFlag = (bidi.getRunLevel(logicalIndex) & 1) == 1 ? Font.LAYOUT_RIGHT_TO_LEFT :
                            Font.LAYOUT_LEFT_TO_RIGHT;
                    advance = layoutStyle(glyphList, text, start + bidi.getRunStart(logicalIndex),
                            start + bidi.getRunLimit(logicalIndex),
                            layoutFlag, advance, codes);
                }
            }

            return advance;
        }

        /* If text is entirely left-to-right, then insert an EntryText node for the entire string */
        else {
            return layoutStyle(glyphList, text, start, limit, Font.LAYOUT_LEFT_TO_RIGHT, advance, codes);
        }
    }

    @Deprecated
    private float layoutStyle(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags,
                              float advance, FormattingCode[] codes) {
        int currentFontStyle = Font.PLAIN;

        /* Find FormattingCode object with stripIndex <= start; that will have the font style in effect at the
        beginning of this text run */
        //int codeIndex = Arrays.binarySearch(codes, start);

        /*
         * If no exact match is found, Arrays.binarySearch() returns (-(insertion point) - 1) where the insertion
         * point is the index
         * of the first FormattingCode with a stripIndex > start. In that case, colorIndex is adjusted to select the
         * immediately preceding
         * FormattingCode whose stripIndex < start.
         */
        /*if (codeIndex < 0) {
            codeIndex = -codeIndex - 2;
        }*/

        /* Break up the string into segments, where each segment has the same font style in use */
        //while (start < limit) {
        //int next = limit;

        /* In case of multiple consecutive color codes with the same stripIndex, select the last one which will have
        active font style */
            /*while (codeIndex >= 0 && codeIndex < (codes.length - 1) && codes[codeIndex].stripIndex ==
            codes[codeIndex + 1].stripIndex) {
                codeIndex++;
            }*/

        /* If an actual FormattingCode object was found (colorIndex within the array), use its fontStyle for layout
        and render */
            /*if (codeIndex >= 0 && codeIndex < codes.length) {
                currentFontStyle = codes[codeIndex].fontStyle;
            }*/

        /*
         * Search for the next FormattingCode that uses a different fontStyle than the current one. If found, the
         * stripIndex of that
         * new code is the split point where the string must be split into a separately styled segment.
         */
            /*while (++codeIndex < codes.length) {
                if (codes[codeIndex].fontStyle != currentFontStyle) {
                    next = codes[codeIndex].stripIndex;
                    break;
                }
            }*/

        /* Layout the string segment with the style currently selected by the last color code */
        //advance = layoutString(glyphList, text, start, next, layoutFlags, advance, currentFontStyle);
        //start = next;
        //}

        return advance;
    }

    /**
     * Given a string that runs contiguously LTR or RTL, break it up into individual segments based on which fonts can
     * render
     * which characters in the string. Calls layoutFont() for each portion of the string that can be layed out with a
     * single
     * font.
     *
     * @param glyphList   will hold all new Glyph objects allocated by layoutFont()
     * @param text        the string to lay out
     * @param start       the offset into text at which to start the layout
     * @param limit       the (offset + length) at which to stop performing the layout
     * @param layoutFlags either Font.LAYOUT_RIGHT_TO_LEFT or Font.LAYOUT_LEFT_TO_RIGHT
     * @param advance     the horizontal advance (i.e. X position) returned by previous call to layoutString()
     * @param style       combination of PLAIN, BOLD, and ITALIC to select a fonts with some specific style
     * @return the advance (horizontal distance) of this string plus the advance passed in as an argument
     */
    @Deprecated
    private float layoutString(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags,
                               float advance, int style) {
        /*
         * Convert all digits in the string to a '0' before layout to ensure that any glyphs replaced on the fly will
         *  all have
         * the same positions. Under Windows, Java's "SansSerif" logical font uses the "Arial" font for digits, in
         * which the "1"
         * digit is slightly narrower than all other digits. Checking the digitGlyphsReady flag prevents a
         * chicken-and-egg
         * problem where the digit glyphs have to be initially cached and the digitGlyphs[] array initialized without
         *  replacing
         * every digit with '0'.
         */
        /*if (digitGlyphsReady) {
            for (int index = start; index < limit; index++) {
                if (text[index] >= '0' && text[index] <= '9') {
                    text[index] = '0';
                }
            }
        }*/

        /* Break the string up into segments, where each segment can be displayed using a single font */
        while (start < limit) {
            //Font font = glyphManager.lookupFont(text, start, limit, style);
            int next = 0;//font.canDisplayUpTo(text, start, limit);

            /* canDisplayUpTo returns -1 if the entire string range is supported by this font */
            //if (next == -1) {
            //    next = limit;
            //}

            /*
             * canDisplayUpTo() returns start if the starting character is not supported at all. In that case, draw
             * just the
             * one unsupported character (which will use the font's "missing glyph code"), then retry the lookup
             * again at the
             * next character after that.
             */
            if (next == start) {
                next++;
            }

            //advance = layoutFont(glyphList, text, start, next, layoutFlags, advance, font);
            start = next;
        }

        return advance;
    }

    /**
     * Allocate new Glyph objects and add them to the glyph list. This sequence of Glyphs represents a portion of the
     * string where all glyphs run contiguously in either LTR or RTL and come from the same physical/logical font.
     *
     * @param glyphList   all newly created Glyph objects are added to this list
     * @param text        the string to layout
     * @param start       the offset into text at which to start the layout
     * @param limit       the (offset + length) at which to stop performing the layout
     * @param layoutFlags either Font.LAYOUT_RIGHT_TO_LEFT or Font.LAYOUT_LEFT_TO_RIGHT
     * @param advance     the horizontal advance (i.e. X position) returned by previous call to layoutString()
     * @param font        the Font used to layout a GlyphVector for the string
     * @return the advance (horizontal distance) of this string plus the advance passed in as an argument
     */
    @Deprecated
    private float layoutFont(List<Glyph> glyphList, char[] text, int start, int limit, int layoutFlags, float advance
            , Font font) {
        /*
         * Ensure that all glyphs used by the string are pre-rendered and cached in the texture. Only safe to do so
         * from the
         * main thread because cacheGlyphs() can crash LWJGL if it makes OpenGL calls from any other thread. In this
         * case,
         * cacheString() will also not insert the entry into the stringCache since it may be incomplete if lookupGlyph()
         * returns null for any glyphs not yet stored in the glyph cache.
         */
        //if (mainThread == Thread.currentThread()) { // already checked
        //glyphManager.cacheGlyphs(font, text, start, limit, layoutFlags);
        //}

        /* Creating a GlyphVector takes care of all language specific OpenType glyph substitutions and positionings */
        GlyphVector vector = null;//glyphManager.layoutGlyphVector(font, text, start, limit, layoutFlags);

        /*
         * Extract all needed information for each glyph from the GlyphVector so it won't be needed for actual
         * rendering.
         * Note that initially, glyph.start holds the character index into the stripped text array. But after the entire
         * string is layed out, this field will be adjusted on every Glyph object to correctly index the original
         * unstripped
         * string.
         */
        Glyph glyph = null;
        int numGlyphs = 1;//vector.getNumGlyphs();
        //for (int index = 0; index < numGlyphs; index++) {
        //Point position = vector.getGlyphPixelBounds(index, null, advance, 0).getLocation();

        /* Compute horizontal advance for the previous glyph based on this glyph's position */
            /*if (glyph != null) {
                glyph.advance = position.x - glyph.x;
            }*/

        /*
         * Allocate a new glyph object and add to the glyphList. The glyph.stringIndex here is really like stripIndex
         *  but
         * it will be corrected later to account for the color codes that have been stripped out.
         */
            /*glyph = new GlyphInfo();
            glyph.stringIndex = start + vector.getGlyphCharIndex(index);
            glyph.texture = glyphManager.lookupGlyph(font, vector.getGlyphCode(index));
            glyph.x = position.x;
            glyph.y = position.y;
            glyphList.add(glyph);*/
        //}

        /* Compute the advance position of the last glyph (or only glyph) since it can't be done by the above loop */
        /*advance += vector.getGlyphPosition(numGlyphs).getX();
        if (glyph != null) {
            glyph.advance = advance - glyph.x;
        }*/

        /* Return the overall horizontal advance in pixels from the start of string */
        return advance;
    }

    /**
     * This entry holds the laid out glyph positions for the cached string along with some relevant metadata.
     */
    @Deprecated
    private static class Entry {

        /**
         * A weak reference back to the Key object in stringCache that maps to this Entry.
         */
        public WeakReference<Key> keyRef; // We do not use this anymore

        /**
         * The total horizontal advance (i.e. width) for this string in pixels.
         */
        public float advance;

        /**
         * Array of fully layed out glyphs for the string. Sorted by logical order of characters (i.e.
         * glyph.stringIndex)
         */
        public Glyph[] glyphs;

        /**
         * Array of color code locations from the original string
         */
        public FormattingCode[] codes;

        /**
         * True if the string uses strikethrough or underlines anywhere and needs an extra pass in renderString()
         */
        public boolean needExtraRender;
    }

    /**
     * Identifies the location and value of a single color code in the original string
     */
    @Deprecated
    private static class FormattingCode implements Comparable<Integer> {

        /**
         * Bit flag used with renderStyle to request the underline style
         */
        public static final byte UNDERLINE = 1;

        /**
         * Bit flag used with renderStyle to request the strikethrough style
         */
        public static final byte STRIKETHROUGH = 2;

        /**
         * The index into the original string (i.e. with color codes) for the location of this color code.
         */
        public int stringIndex;

        /**
         * The index into the stripped string (i.e. with no color codes) of where this color code would have appeared
         */
        public int stripIndex;

        /**
         * Combination of PLAIN, BOLD, and ITALIC specifying font specific styles
         */
        public byte fontStyle;

        /**
         * The numeric color code (i.e. index into the colorCode[] array); -1 to reset default (original parameter)
         * color
         */
        @Nullable
        public Color3i color;

        /**
         * Combination of UNDERLINE and STRIKETHROUGH flags specifying effects performed by renderString()
         */
        public byte renderEffect;

        /**
         * Performs numeric comparison on stripIndex. Allows binary search on ColorCode arrays in layoutStyle.
         *
         * @param i the Integer object being compared
         * @return either -1, 0, or 1 if this < other, this == other, or this > other
         */
        @Override
        public int compareTo(@Nonnull Integer i) {
            return Integer.compare(stringIndex, i);
        }
    }

    @Deprecated
    private static class Glyph implements Comparable<Glyph> {

        /**
         * The index into the original string (i.e. with color codes) for the character that generated this glyph.
         */
        int stringIndex;

        /**
         * Texture ID and position/size of the glyph's pre-rendered image within the cache texture.
         */
        int texture;

        /**
         * Glyph's horizontal position (in pixels) relative to the entire string's baseline
         */
        int x;

        /**
         * Glyph's vertical position (in pixels) relative to the entire string's baseline
         */
        int y;

        /**
         * Glyph's horizontal advance (in pixels) used for strikethrough and underline effects
         */
        float advance;

        /**
         * Allows arrays of Glyph objects to be sorted. Performs numeric comparison on stringIndex.
         *
         * @param o the other Glyph object being compared with this one
         * @return either -1, 0, or 1 if this < other, this == other, or this > other
         */
        @Override
        public int compareTo(Glyph o) {
            return Integer.compare(stringIndex, o.stringIndex);
        }
    }

    /**
     * Wraps a String and acts as the key into stringCache. The hashCode() and equals() methods consider all ASCII
     * digits
     * to be equal when hashing and comparing Key objects together. Therefore, Strings which only differ in their digits
     * will
     * be all hashed together into the same entry. The renderString() method will then substitute the correct digit
     * glyph on
     * the fly. This special digit handling gives a significant speedup on the F3 debug screen.
     */
    @Deprecated
    private static class Key {

        /**
         * A copy of the String which this Key is indexing. A copy is used to avoid creating a strong reference to the
         * original
         * passed into renderString(). When the original String is no longer needed by Minecraft, it will be garbage
         * collected
         * and the WeakHashMaps in StringCache will allow this Key object and its associated Entry object to be garbage
         * collected as well.
         */
        public String str;

        /**
         * Computes a hash code on str in the same manner as the String class, except all ASCII digits hash as '0'
         *
         * @return the augmented hash code on str
         */
        @Override
        public int hashCode() {
            int code = 0, length = str.length();

            /*
             * True if a section mark character was last seen. In this case, if the next character is a digit, it must
             * not be considered equal to any other digit. This forces any string that differs in color codes only to
             * have a separate entry in the StringCache.
             */
            boolean colorCode = false;

            for (int index = 0; index < length; index++) {
                char c = str.charAt(index);
                if (c >= '0' && c <= '9' && !colorCode) {
                    c = '0';
                }
                code = (code * 31) + c;
                colorCode = (c == '\u00A7');
            }

            return code;
        }

        /**
         * Compare str against another object (specifically, the object's string representation as returned by
         * toString).
         * All ASCII digits are considered equal by this method, as long as they are at the same index within the
         * string.
         *
         * @return true if the strings are the identical, or only differ in their ASCII digits
         */
        @Override
        public boolean equals(Object o) {
            /*
             * There seems to be a timing window inside WeakHashMap itself where a null object can be passed to this
             * equals() method. Presumably it happens between computing a hash code for the weakly referenced Key object
             * while it still exists and calling its equals() method after it was garbage collected.
             */
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            /* Calling toString on a String object simply returns itself so no new object allocation is performed */
            String other = o.toString();
            int length = str.length();

            if (length != other.length()) {
                return false;
            }

            /*
             * True if a section mark character was last seen. In this case, if the next character is a digit, it must
             * not be considered equal to any other digit. This forces any string that differs in color codes only to
             * have a separate entry in the StringCache.
             */
            boolean colorCode = false;

            for (int index = 0; index < length; index++) {
                char c1 = str.charAt(index);
                char c2 = other.charAt(index);

                if (c1 != c2 && (c1 < '0' || c1 > '9' || c2 < '0' || c2 > '9' || colorCode)) {
                    return false;
                }
                colorCode = (c1 == '\u00A7');
            }

            return true;
        }

        /**
         * Returns the contained String object within this Key.
         *
         * @return the str object
         */
        @Override
        public String toString() {
            return str;
        }
    }
}
