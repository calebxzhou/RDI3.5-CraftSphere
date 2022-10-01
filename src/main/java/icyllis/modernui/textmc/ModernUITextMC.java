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

import icyllis.modernui.ModernUI;
import icyllis.modernui.core.Core;
import icyllis.modernui.graphics.font.GlyphManager;
import icyllis.modernui.text.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import javax.annotation.Nonnull;

import static icyllis.modernui.ModernUI.*;

/**
 * Modern UI Text MC can bootstrap independently.
 */
public final class ModernUITextMC {


    public static Config CONFIG=new Config();

    private ModernUITextMC() {
    }

    public static void init() {
    }

    public static void initConfig() {
        /*FMLPaths.getOrCreateGameRelativePath(FMLPaths.CONFIGDIR.get().resolve(ModernUI.NAME_CPT), ModernUI.NAME_CPT);
        ModContainer mod = ModLoadingContext.get().getActiveContainer();

        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        CONFIG = new Config(builder);
        CONFIG_SPEC = builder.build();
        mod.addConfig(new ModConfig(ModConfig.Type.CLIENT, CONFIG_SPEC, mod, ModernUI.NAME_CPT + "/text.toml"));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(CONFIG::onReload);*/
    }


    public static void setupClient() {
		LOGGER.info("Setup Modern UI");
		TextLayoutEngine.getInstance().cleanup();
        // preload text engine, note that this event is fired after client config first load
        // so that the typeface config is valid
        Minecraft.getInstance().execute(ModernUI::getSelectedTypeface);
        /*MuiForgeApi.addOnWindowResizeListener((width, height, newScale, oldScale) -> {
            if (Core.hasRenderThread() && newScale != oldScale) {
                TextLayoutEngine.getInstance().reload();
				LOGGER.debug(MARKER, "Registered language reload listener");
            }
        });*/
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.register((server, oldResourceManager) -> {
			// language may reload, cause TranslatableComponent changed, so clear layout cache
			TextLayoutEngine.getInstance().reloadResources();
		});
		CrashReportEvents.SYSTEM_DETAILS.register(details -> {
			details.setDetail("CacheCount",TextLayoutEngine.getInstance().getCacheCount()+"");
			int memorySize = TextLayoutEngine.getInstance().getCacheMemorySize();
			details.setDetail("CacheSize", TextUtils.binaryCompact(memorySize) + " (" + memorySize + " bytes)");


			memorySize = TextLayoutEngine.getInstance().getEmojiAtlasMemorySize();
			details.setDetail("EmojiAtlasSize", TextUtils.binaryCompact(memorySize) + " (" + memorySize + " bytes)");
			GlyphManager.getInstance().dumpInfo(details);

		});
        {
            int[] codePoints = {0x1f469, 0x1f3fc, 0x200d, 0x2764, 0xfe0f, 0x200d, 0x1f48b, 0x200d, 0x1f469, 0x1f3fd};
            CharSequenceBuilder builder = new CharSequenceBuilder();
            for (int cp : codePoints) {
                builder.addCodePoint(cp);
            }
            String string = new String(codePoints, 0, codePoints.length);
            if (builder.hashCode() != string.hashCode() || builder.hashCode() != builder.toString().hashCode()) {
                throw new RuntimeException("String.hashCode() is not identical to the specs");
            }
        }
        //MinecraftForge.EVENT_BUS.register(EventHandler.class);
        LOGGER.info(MARKER, "Loaded modern text engine");
    }

    /*@SubscribeEvent
    static void onParallelDispatch(@Nonnull ParallelDispatchEvent event) {
        // since Forge EVENT_BUS is not started yet, we should manually maintain that
        // in case of some mods render texts before entering main menu
        event.enqueueWork(() -> TextLayoutEngine.getInstance().cleanup());
    }*/

    /*@OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    static void registerShaders(@Nonnull RegisterShadersEvent event) {
        ResourceProvider provider = event.getResourceManager();
        try {
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShader);
            event.registerShader(new ShaderInstance(provider, TextRenderType.SHADER_SEE_THROUGH_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP), TextRenderType::setShaderSeeThrough);
        } catch (IOException e) {
            throw new RuntimeException("Bad shaders", e);
        }
    }*/

    /*static class EventHandler {

        @SubscribeEvent
        static void onClientChat(@Nonnull ClientChatEvent event) {
            final String msg = event.getMessage();
            if (CONFIG.mEmojiShortcodes.get() && !msg.startsWith("/")) {
                final TextLayoutEngine engine = TextLayoutEngine.getInstance();
                final Matcher matcher = TextLayoutEngine.EMOJI_SHORTCODE_PATTERN.matcher(msg);

                StringBuilder builder = null;
                int lastEnd = 0;
                while (matcher.find()) {
                    if (builder == null) {
                        builder = new StringBuilder();
                    }
                    int st = matcher.start();
                    int en = matcher.end();
                    String emoji = null;
                    if (en - st > 2) {
                        emoji = engine.lookupEmojiShortcode(msg.substring(st + 1, en - 1));
                    }
                    if (emoji != null) {
                        builder.append(msg, lastEnd, st);
                        builder.append(emoji);
                    } else {
                        builder.append(msg, lastEnd, en);
                    }
                    lastEnd = en;
                }
                if (builder != null) {
                    builder.append(msg, lastEnd, msg.length());
                    //event.setMessage(builder.toString());
                }
            }
        }
    }*/

    public static class Config {

        public static final float BASE_FONT_SIZE_MIN = 6;
        public static final float BASE_FONT_SIZE_MAX = 10;
        public static final float BASELINE_MIN = 4;
        public static final float BASELINE_MAX = 10;
        public static final float SHADOW_OFFSET_MIN = 0.2f;
        public static final float SHADOW_OFFSET_MAX = 4;
        public static final float OUTLINE_OFFSET_MIN = 0.2f;
        public static final float OUTLINE_OFFSET_MAX = 4;
        public static final int LIFESPAN_MIN = 2;
        public static final int LIFESPAN_MAX = 60;
        public static final int REHASH_MIN = 0;
        public static final int REHASH_MAX = 2000;

        //final boolean globalRenderer;
        public final boolean mAllowShadow = true;
        public final boolean mFixedResolution = false;
        public final double mBaseFontSize=12;
        public final double mBaselineShift=7;
        public final double mShadowOffset=0.8;
        public final double mOutlineOffset=0.5;
        public final boolean mSuperSampling=true;
        public final boolean mAlignPixels=false;
        public final int mCacheLifespan=12;
        public final int mRehashThreshold=100;
        public final int mTextDirection=1;
        public final boolean mColorEmoji=true;
        public final boolean mBitmapReplacement=false;
        public final boolean mEmojiShortcodes=true;

        //private final boolean antiAliasing;
        //private final boolean highPrecision;
        //private final boolean enableMipmap;
        //private final int mipmapLevel;
        //private final int resolutionLevel;
        //private final int defaultFontSize;

        private Config(/*@Nonnull ForgeConfigSpec.Builder builder*/) {
          /*  builder.comment("Text Config")
                    .push("text");*/

            /*globalRenderer = builder.comment(
                    "Apply Modern UI font renderer (including text layouts) to the entire game rather than only " +
                            "Modern UI itself.")
                    .define("globalRenderer", true);*/
           /* mAllowShadow = builder.comment(
                            "Allow text renderer to drop shadow, setting to false can improve performance.")
                    .define("allowShadow", true);*/
           /* mFixedResolution = builder.comment(
                            "Fix resolution level at 2. When the GUI scale increases, the resolution level remains.",
                            "Then GUI scale should be even numbers (2, 4, 6...), based on Minecraft GUI system.",
                            "If your fonts are not bitmap fonts, then you should keep this setting false.")
                    .define("fixedResolution", false);*/
           /* mBaseFontSize = builder.comment(
                            "Control base font size, in GUI scaled pixels. The default and vanilla value is 8.",
                            "For bitmap fonts, 8 represents a glyph size of 8x or 16x if fixed resolution.")
                    .defineInRange("baseFontSize", 8.0, BASE_FONT_SIZE_MIN, BASE_FONT_SIZE_MAX);
            mBaselineShift = builder.comment(
                            "Control vertical baseline for vanilla text layout, in GUI scaled pixels.",
                            "For smaller font, 6 is recommended. The default value is 7.")
                    .defineInRange("baselineShift", 7.0, BASELINE_MIN, BASELINE_MAX);
            mShadowOffset = builder.comment(
                            "Control the text shadow offset for vanilla text rendering, in GUI scaled pixels.")
                    .defineInRange("shadowOffset", 0.8, SHADOW_OFFSET_MIN, SHADOW_OFFSET_MAX);
            mOutlineOffset = builder.comment(
                            "Control the text outline offset for vanilla text rendering, in GUI scaled pixels.")
                    .defineInRange("outlineOffset", 0.5, OUTLINE_OFFSET_MIN, OUTLINE_OFFSET_MAX);
            mSuperSampling = builder.comment(
                            "Super sampling can make the text more sharper with large font size or in the 3D world.",
                            "But perhaps it makes the path edge too blurry and difficult to read.")
                    .define("superSampling", false);*/
            /*mAlignPixels = builder.comment(
                            "Enable to make each glyph pixel-aligned in text layout in screen space.",
                            "Text rendering may be better with bitmap fonts or fixed resolution or linear sampling.")
                    .define("alignPixels", false);
            mCacheLifespan = builder.comment(
                            "Set the recycle time of layout cache in seconds, using least recently used algorithm.")
                    .defineInRange("cacheLifespan", 12, LIFESPAN_MIN, LIFESPAN_MAX);
            mRehashThreshold = builder.comment("Set the rehash threshold of layout cache")
                    .defineInRange("rehashThreshold", 100, REHASH_MIN, REHASH_MAX);
            mTextDirection = builder.comment(
                            "Control bidirectional text heuristic algorithm.")
                    .defineInRange("textDirection", View.TEXT_DIRECTION_FIRST_STRONG,
                            View.TEXT_DIRECTION_FIRST_STRONG, View.TEXT_DIRECTION_FIRST_STRONG_RTL);
            mColorEmoji = builder.comment(
                            "Whether to render colored emoji or just grayscale emoji.")
                    .define("colorEmoji", true);
            mBitmapReplacement = builder.comment(
                            "Whether to use bitmap replacement for non-Emoji character sequences. Restart is required.")
                    .define("bitmapReplacement", false);
            mEmojiShortcodes = builder.comment(
                            "Allow to use Slack or Discord shortcodes to replace Emoji character sequences in chat.")
                    .define("emojiShortcodes", true);*/
            /*antiAliasing = builder.comment(
                    "Enable font anti-aliasing.")
                    .define("antiAliasing", true);
            highPrecision = builder.comment(
                    "Enable high precision rendering, this is very useful especially when the font is very small.")
                    .define("highPrecision", true);
            enableMipmap = builder.comment(
                    "Enable mipmap for font textures, this makes font will not be blurred when scaling down.")
                    .define("enableMipmap", true);
            mipmapLevel = builder.comment(
                    "The mipmap level for font textures.")
                    .defineInRange("mipmapLevel", 4, 0, 4);*/
            /*resolutionLevel = builder.comment(
                    "The resolution level of font, higher levels would better work with high resolution monitors.",
                    "Reference: 1 (Standard, 1.5K Fullscreen), 2 (High, 2K~3K Fullscreen), 3 (Ultra, 4K Fullscreen)",
                    "This should match your GUI scale. Scale -> Level: [1,2] -> 1; [3,4] -> 2; [5,) -> 3")
                    .defineInRange("resolutionLevel", 2, 1, 3);*/
            /*defaultFontSize = builder.comment(
                    "The default font size for texts with no size specified. (deprecated, to be removed)")
                    .defineInRange("defaultFontSize", 16, 12, 20);*/

           // builder.pop();
        }

      /*  public void saveOnly() {
            Util.ioPool().execute(() -> CONFIG_SPEC.save());
        }

        public void saveAndReload() {
            Util.ioPool().execute(() -> {
                CONFIG_SPEC.save();
                reload();
            });
        }*/

        void onReload(/*@Nonnull ModConfigEvent event*/) {
            /*final IConfigSpec<?> spec = event.getConfig().getSpec();
            if (spec != CONFIG_SPEC) {
                return;
            }*/
            reload();
            LOGGER.debug(MARKER, "Text config reloaded with ");
        }

        void reload() {
            boolean reload = false;
            ModernTextRenderer.sAllowShadow = mAllowShadow;
            if (TextLayoutEngine.sFixedResolution != mFixedResolution) {
                TextLayoutEngine.sFixedResolution = mFixedResolution;
                reload = true;
            }
            if (TextLayoutProcessor.sBaseFontSize != mBaseFontSize) {
                TextLayoutProcessor.sBaseFontSize = (float) mBaseFontSize;
                reload = true;
            }
            TextRenderNode.sBaselineOffset = (float) mBaselineShift;
            ModernTextRenderer.sShadowOffset = (float) mShadowOffset;
            ModernTextRenderer.sOutlineOffset = (float) mOutlineOffset;
            if (TextLayoutEngine.sSuperSampling != mSuperSampling) {
                TextLayoutEngine.sSuperSampling = mSuperSampling;
                reload = true;
            }
            if (TextLayoutProcessor.sAlignPixels != mAlignPixels) {
                TextLayoutProcessor.sAlignPixels = mAlignPixels;
                reload = true;
            }
            TextLayoutEngine.sCacheLifespan = mCacheLifespan;
            TextLayoutEngine.sRehashThreshold = mRehashThreshold;
            if (TextLayoutEngine.sTextDirection != mTextDirection) {
                TextLayoutEngine.sTextDirection = mTextDirection;
                reload = true;
            }
            if (TextLayoutProcessor.sColorEmoji != mColorEmoji) {
                TextLayoutProcessor.sColorEmoji = mColorEmoji;
                reload = true;
            }
            if (reload) {
                Minecraft.getInstance().submit(() -> TextLayoutEngine.getInstance().reload());
            }
            //GlyphManagerForge.getInstance().reload();
            /*GlyphManagerForge.sAntiAliasing = antiAliasing.get();
            GlyphManagerForge.sHighPrecision = highPrecision.get();
            GlyphManagerForge.sEnableMipmap = enableMipmap.get();
            GlyphManagerForge.sMipmapLevel = mipmapLevel.get();*/
            //GlyphManager.sResolutionLevel = resolutionLevel.get();
            //TextLayoutEngine.sDefaultFontSize = defaultFontSize.get();
        }
    }
}
