package calebzhou.rdi.core.client.screen;

import calebzhou.rdi.core.client.emojiful.EmojiClientProxy;
import calebzhou.rdi.core.client.emojiful.gui.EmojiSelectionGui;
import calebzhou.rdi.core.client.emojiful.gui.EmojiSuggestionHelper;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.chat.ChatPreviewAnimator;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.client.multiplayer.chat.ChatPreviewStatus;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.PreviewableCommand;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class RdiChatScreen extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int PREVIEW_MARGIN_SIDES = 2;
    private static final int PREVIEW_PADDING = 2;
    private static final int PREVIEW_MARGIN_BOTTOM = 15;
    private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
    private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
    private static final Component PREVIEW_INPUT_HINT = Component.translatable("chat.previewInput", Component.translatable("key.keyboard.enter"))
            .withStyle(ChatFormatting.DARK_GRAY);
    private String historyBuffer = "";
    private int historyPos = -1;
    public RdiChatEditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;
    private ClientChatPreview chatPreview;
    private ChatPreviewStatus chatPreviewStatus;
    private boolean previewNotRequired;
    private final ChatPreviewAnimator chatPreviewAnimator = new ChatPreviewAnimator();

    public RdiChatScreen(String string) {
        super(Component.literal(""));
        this.initial = string;
    }

    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new RdiChatEditBox(this.font, 4, this.height - 12, this.width - 4, 12, Component.translatable("chat.editBox")) {
            protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(RdiChatScreen.this.commandSuggestions.getNarrationMessage());
            }
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.addWidget(this.input);
        this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
        this.commandSuggestions.updateCommandInfo();
        EmojiClientProxy.emojiSuggestionHelper = new EmojiSuggestionHelper(this);
        EmojiClientProxy.emojiSelectionGui = new EmojiSelectionGui(this);
        EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);
        this.setInitialFocus(this.input);
        this.chatPreview = new ClientChatPreview(this.minecraft);
        this.updateChatPreview(this.input.getValue());
        ServerData serverData = this.minecraft.getCurrentServer();
        this.chatPreviewStatus = serverData != null && !serverData.previewsChat() ? ChatPreviewStatus.OFF : this.minecraft.options.chatPreview().get();
        if (serverData != null && this.chatPreviewStatus != ChatPreviewStatus.OFF) {
            ServerData.ChatPreview chatPreview = serverData.getChatPreview();
            if (chatPreview != null && serverData.previewsChat() && chatPreview.showToast()) {
                ServerList.saveSingleServer(serverData);
                SystemToast systemToast = SystemToast.multiline(
                        this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST
                );
                this.minecraft.getToasts().addToast(systemToast);
            }
        }

        if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM) {
            this.previewNotRequired = this.initial.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(this.initial.substring(1));
        }

    }

    public void resize(Minecraft minecraft, int width, int height) {
        String string = this.input.getValue();
        this.init(minecraft, width, height);
        this.setChatLine(string);
        this.commandSuggestions.updateCommandInfo();
        EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.gui.getChat().resetChatScroll();
    }

    public void tick() {
        this.input.tick();
        this.chatPreview.tick();
    }

    private void onEdited(String value) {
        String string = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!string.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
        if (this.chatPreviewStatus == ChatPreviewStatus.LIVE) {
            this.updateChatPreview(string);
        } else if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.chatPreview.queryEquals(string)) {
            this.previewNotRequired = string.startsWith("/") && !this.minecraft.player.commandHasSignableArguments(string.substring(1));
            this.chatPreview.update("");
        }
        EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);
        this.updateChatPreview(string);
    }

    private void updateChatPreview(String string) {
        String string2 = this.normalizeChatMessage(string);
        if (this.sendsChatPreviewRequests()) {
            this.requestPreview(string2);
        } else {
            this.chatPreview.disable();
        }

    }

    private void requestPreview(String message) {
        if (message.startsWith("/")) {
            this.requestCommandArgumentPreview(message);
        } else {
            this.requestChatMessagePreview(message);
        }

    }

    private void requestChatMessagePreview(String string) {
        this.chatPreview.update(string);
    }

    private void requestCommandArgumentPreview(String string) {
        ParseResults<SharedSuggestionProvider> parseResults = this.commandSuggestions.getCurrentContext();
        CommandNode<SharedSuggestionProvider> commandNode = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
        if (parseResults != null && commandNode != null && PreviewableCommand.of(parseResults).isPreviewed(commandNode)) {
            this.chatPreview.update(string);
        } else {
            this.chatPreview.disable();
        }

    }

    private boolean sendsChatPreviewRequests() {
        if (this.minecraft.player == null) {
            return false;
        } else if (this.minecraft.isLocalServer()) {
            return true;
        } else if (this.chatPreviewStatus == ChatPreviewStatus.OFF) {
            return false;
        } else {
            ServerData serverData = this.minecraft.getCurrentServer();
            return serverData != null && serverData.previewsChat();
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (EmojiClientProxy.emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (EmojiClientProxy.emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 256) {
            this.minecraft.setScreen((Screen) null);
            return true;
        } else if (keyCode != 257 && keyCode != 335) {
            if (keyCode == 265) {
                this.moveInHistory(-1);
                return true;
            } else if (keyCode == 264) {
                this.moveInHistory(1);
                return true;
            } else if (keyCode == 266) {
                this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
                return true;
            } else if (keyCode == 267) {
                this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
                return true;
            } else {
                return false;
            }
        } else {
            this.handleChatInput(this.input.getValue(), true);
            this.minecraft.setScreen((Screen) null);
            return true;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        delta = Mth.clamp(delta, -1.0, 1.0);
        if (this.commandSuggestions.mouseScrolled(delta)) {
            return true;
        } else if (EmojiClientProxy.emojiSelectionGui.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        } else {
            if (!hasShiftDown()) {
                delta *= MOUSE_SCROLL_SPEED;
            }

            this.minecraft.gui.getChat().scrollChat((int) delta);
            return true;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        EmojiClientProxy.emojiSelectionGui.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestions.mouseClicked((double) ((int) mouseX), (double) ((int) mouseY), button)) {
            return true;
        } else if (EmojiClientProxy.emojiSelectionGui.mouseClicked(mouseX, mouseY))
            return true;
        else {
            if (button == 0) {
                ChatComponent chatComponent = this.minecraft.gui.getChat();
                if (chatComponent.handleChatQueueClicked(mouseX, mouseY)) {
                    return true;
                }

                Style style = this.getComponentStyleAt(mouseX, mouseY);
                if (style != null && this.handleComponentClicked(style)) {
                    this.initial = this.input.getValue();
                    return true;
                }
            }

            return this.input.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
        }
    }

    protected void insertText(String text, boolean overwrite) {
        if (overwrite) {
            this.input.setValue(text);
        } else {
            this.input.insertText(text);
        }

    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next message from the current cursor position
     */
    public void moveInHistory(int msgPos) {
        int i = this.historyPos + msgPos;
        int j = this.minecraft.gui.getChat().getRecentChat().size();
        i = Mth.clamp((int) i, (int) 0, (int) j);
        if (i != this.historyPos) {
            if (i == j) {
                this.historyPos = j;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == j) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue((String) this.minecraft.gui.getChat().getRecentChat().get(i));
                this.commandSuggestions.setAllowSuggestions(false);
                this.historyPos = i;
            }
        }
    }

    public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        this.setFocused(this.input);
        this.input.setFocus(true);
        fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(poseStack, mouseX, mouseY, partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
        boolean bl = this.minecraft.getProfileKeyPairManager().signer() != null;
        ChatPreviewAnimator.State state = this.chatPreviewAnimator.get(Util.getMillis(), this.getDisplayedPreviewText());
        if (state.preview() != null) {
            this.renderChatPreview(poseStack, state.preview(), state.alpha(), bl);
            this.commandSuggestions.renderSuggestions(poseStack, mouseX, mouseY);
        } else {
            this.commandSuggestions.render(poseStack, mouseX, mouseY);
            EmojiClientProxy.emojiSuggestionHelper.render(poseStack);
            if (bl) {
                poseStack.pushPose();
                fill(poseStack, 0, this.height - 14, 2, this.height - 2, -8932375);
                poseStack.popPose();
            }
        }

        Style style = this.getComponentStyleAt((double) mouseX, (double) mouseY);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        } else {
            GuiMessageTag guiMessageTag = this.minecraft.gui.getChat().getMessageTagAt((double) mouseX, (double) mouseY);
            if (guiMessageTag != null && guiMessageTag.text() != null) {
                this.renderTooltip(poseStack, this.font.split(guiMessageTag.text(), 260), mouseX, mouseY);
            }
        }

        EmojiClientProxy.emojiSelectionGui.render(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
    }

    @Nullable
    protected Component getDisplayedPreviewText() {
        String string = this.input.getValue();
        if (string.isBlank()) {
            return null;
        } else {
            Component component = this.peekPreview();
            return this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired
                    ? (Component) Objects.requireNonNullElse(
                    component, this.chatPreview.queryEquals(string) && !string.startsWith("/") ? Component.literal(string) : PREVIEW_INPUT_HINT
            )
                    : component;
        }
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String chatLine) {
        this.input.setValue(chatLine);
    }

    protected void updateNarrationState(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getTitle());
        output.add(NarratedElementType.USAGE, USAGE_TEXT);
        String string = this.input.getValue();
        if (!string.isEmpty()) {
            output.nest().add(NarratedElementType.TITLE, (Component) Component.translatable("chat_screen.message", string));
        }

    }

    public void renderChatPreview(PoseStack poseStack, Component message, float alpha, boolean signable) {
        int i = (int) (255.0 * (this.minecraft.options.chatOpacity().get() * 0.9F + 0.1F) * (double) alpha);
        int j = (int) ((double) (this.chatPreview.hasScheduledRequest() ? 127 : 255) * this.minecraft.options.textBackgroundOpacity().get() * (double) alpha);
        int k = this.chatPreviewWidth();
        List<FormattedCharSequence> list = this.splitChatPreview(message);
        int l = this.chatPreviewHeight(list);
        int m = this.chatPreviewTop(l);
        RenderSystem.enableBlend();
        poseStack.pushPose();
        poseStack.translate((double) this.chatPreviewLeft(), (double) m, 0.0);
        fill(poseStack, 0, 0, k, l, j << 24);
        if (i > 0) {
            poseStack.translate(2.0, 2.0, 0.0);

            for (int n = 0; n < list.size(); ++n) {
                FormattedCharSequence formattedCharSequence = (FormattedCharSequence) list.get(n);
                int o = n * 9;
                this.renderChatPreviewHighlights(poseStack, formattedCharSequence, o, i);
                this.font.drawShadow(poseStack, formattedCharSequence, 0.0F, (float) o, i << 24 | 16777215);
            }
        }

        poseStack.popPose();
        RenderSystem.disableBlend();
        if (signable && this.chatPreview.peek() != null) {
            int n = this.chatPreview.hasScheduledRequest() ? 15118153 : 7844841;
            int p = (int) (255.0F * alpha);
            poseStack.pushPose();
            fill(poseStack, 0, m, 2, this.chatPreviewBottom(), p << 24 | n);
            poseStack.popPose();
        }

    }

    private void renderChatPreviewHighlights(PoseStack poseStack, FormattedCharSequence text, int minY, int alpha) {
        int i = minY + 9;
        int j = alpha << 24 | 10533887;
        Predicate<Style> predicate = style -> style.getHoverEvent() != null || style.getClickEvent() != null;

        for (StringSplitter.Span span : this.font.getSplitter().findSpans(text, predicate)) {
            int k = Mth.floor(span.left());
            int l = Mth.ceil(span.right());
            fill(poseStack, k, minY, l, i, j);
        }

    }

    @Nullable
    private Style getComponentStyleAt(double d, double e) {
        Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(d, e);
        if (style == null) {
            style = this.getChatPreviewStyleAt(d, e);
        }

        return style;
    }

    @Nullable
    private Style getChatPreviewStyleAt(double mouseX, double mouseY) {
        if (this.minecraft.options.hideGui) {
            return null;
        } else {
            Component component = this.peekPreview();
            if (component == null) {
                return null;
            } else {
                List<FormattedCharSequence> list = this.splitChatPreview(component);
                int i = this.chatPreviewHeight(list);
                if (!(mouseX < (double) this.chatPreviewLeft())
                        && !(mouseX > (double) this.chatPreviewRight())
                        && !(mouseY < (double) this.chatPreviewTop(i))
                        && !(mouseY > (double) this.chatPreviewBottom())) {
                    int j = this.chatPreviewLeft() + 2;
                    int k = this.chatPreviewTop(i) + 2;
                    int l = (Mth.floor(mouseY) - k) / 9;
                    if (l >= 0 && l < list.size()) {
                        FormattedCharSequence formattedCharSequence = (FormattedCharSequence) list.get(l);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, (int) (mouseX - (double) j));
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            }
        }
    }

    @Nullable
    private Component peekPreview() {
        return Util.mapNullable(this.chatPreview.peek(), ClientChatPreview.Preview::response);
    }

    private List<FormattedCharSequence> splitChatPreview(Component message) {
        return this.font.split(message, this.chatPreviewWidth());
    }


    private int chatPreviewWidth() {
        return this.minecraft.screen.width - 4;
    }

    private int chatPreviewHeight(List<FormattedCharSequence> list) {
        int var10000 = Math.max(list.size(), 1);
        Objects.requireNonNull(this.font);
        return var10000 * 9 + 4;
    }

    private int chatPreviewBottom() {
        return this.minecraft.screen.height - PREVIEW_MARGIN_BOTTOM;
    }

    private int chatPreviewTop(int height) {
        return this.chatPreviewBottom() - height;
    }

    private int chatPreviewLeft() {
        return PREVIEW_MARGIN_SIDES;
    }

    private int chatPreviewRight() {
        return this.minecraft.screen.width - PREVIEW_MARGIN_SIDES;
    }

    public boolean handleChatInput(String message, boolean addToChat) {
        message = this.normalizeChatMessage(message);
        if (message.isEmpty()) {
            return true;
        } else {
            if (this.chatPreviewStatus == ChatPreviewStatus.CONFIRM && !this.previewNotRequired) {
                this.commandSuggestions.hide();
                if (!this.chatPreview.queryEquals(message)) {
                    this.updateChatPreview(message);
                    return false;
                }
            }

           /* if (addToChat) {
                this.minecraft.gui.getChat().addRecentChat(message);
            }*/

            Component component = Util.mapNullable(this.chatPreview.pull(message), ClientChatPreview.Preview::response);
            if (message.startsWith("/")) {
                this.minecraft.player.commandSigned(message.substring(1), component);
            } else {
                this.minecraft.player.chatSigned(message, component);
            }

            return true;
        }
    }


    public String normalizeChatMessage(String message) {
        return StringUtils.normalizeSpace(message.trim());
    }

    public ClientChatPreview getChatPreview() {
        return this.chatPreview;
    }
}
