package calebzhou.rdi.craftsphere.screen;

import calebzhou.rdi.craftsphere.emojiful.EmojiClientProxy;
import calebzhou.rdi.craftsphere.emojiful.gui.EmojiSelectionGui;
import calebzhou.rdi.craftsphere.emojiful.gui.EmojiSuggestionHelper;
import calebzhou.rdi.craftsphere.emojiful.render.EmojiFontRenderer;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.chat.ClientChatPreview;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.PreviewedArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class RdiChatScreen extends Screen {
    public static final double MOUSE_SCROLL_SPEED = 7.0;
    private static final Component USAGE_TEXT = Component.translatable("chat_screen.usage");
    private static final int PREVIEW_MARGIN_SIDES = 2;
    private static final int PREVIEW_PADDING = 2;
    private static final int PREVIEW_MARGIN_BOTTOM = 15;
    private static final Component PREVIEW_WARNING_TITLE = Component.translatable("chatPreview.warning.toast.title");
    private static final Component PREVIEW_WARNING_TOAST = Component.translatable("chatPreview.warning.toast");
    private static final Component PREVIEW_HINT;
    private String historyBuffer = "";
    private int historyPos = -1;
    public RdiChatEditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;
    private ClientChatPreview chatPreview;

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
        if (serverData != null && (Boolean)this.minecraft.options.chatPreview().get()) {
            ServerData.ChatPreview chatPreview = serverData.getChatPreview();
            if (chatPreview != null && serverData.previewsChat() && chatPreview.showToast()) {
                ServerList.saveSingleServer(serverData);
                SystemToast systemToast = SystemToast.multiline(this.minecraft, SystemToast.SystemToastIds.CHAT_PREVIEW_WARNING, PREVIEW_WARNING_TITLE, PREVIEW_WARNING_TOAST);
                this.minecraft.getToasts().addToast(systemToast);
            }
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
        CommandNode<SharedSuggestionProvider> commandNode = this.commandSuggestions.getNodeAt(this.input.getCursorPosition());
        if (commandNode != null && PreviewedArgument.isPreviewed(commandNode)) {
            this.chatPreview.update(string);
        } else {
            this.chatPreview.disable();
        }

    }

    private boolean sendsChatPreviewRequests() {
        if (this.minecraft.player == null) {
            return false;
        } else if (!(Boolean)this.minecraft.options.chatPreview().get()) {
            return false;
        } else {
            ServerData serverData = this.minecraft.getCurrentServer();
            return serverData != null && serverData.previewsChat();
        }
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }else if(EmojiClientProxy.emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }else if(EmojiClientProxy.emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (keyCode == 256) {
            this.minecraft.setScreen((Screen)null);
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
            this.minecraft.setScreen((Screen)null);
            return true;
        }
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        delta = Mth.clamp(delta, -1.0, 1.0);
        if (this.commandSuggestions.mouseScrolled(delta)) {
            return true;
        } else  if(EmojiClientProxy.emojiSelectionGui.mouseScrolled(mouseX, mouseY, delta)){
            return true;
        }else{
            if (!hasShiftDown()) {
                delta *= MOUSE_SCROLL_SPEED;
            }

            this.minecraft.gui.getChat().scrollChat((int)delta);
            return true;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        EmojiClientProxy.emojiSelectionGui.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestions.mouseClicked((double)((int)mouseX), (double)((int)mouseY), button)) {
            return true;
        }else if(EmojiClientProxy.emojiSelectionGui.mouseClicked(mouseX, mouseY))
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

            return this.input.mouseClicked(mouseX, mouseY, button) ? true : super.mouseClicked(mouseX, mouseY, button);
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
        i = Mth.clamp((int)i, (int)0, (int)j);
        if (i != this.historyPos) {
            if (i == j) {
                this.historyPos = j;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == j) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue((String)this.minecraft.gui.getChat().getRecentChat().get(i));
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
        if (this.chatPreview.isEnabled()) {
            this.renderChatPreview(poseStack);
        } else {
            this.commandSuggestions.render(poseStack, mouseX, mouseY);
            EmojiClientProxy.emojiSuggestionHelper.render(poseStack);
        }

        Style style = this.getComponentStyleAt((double)mouseX, (double)mouseY);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(poseStack, style, mouseX, mouseY);
        }
        EmojiClientProxy.emojiSelectionGui.render(poseStack,mouseX,mouseY,partialTick);
        super.render(poseStack, mouseX, mouseY, partialTick);
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
            output.nest().add(NarratedElementType.TITLE, (Component)Component.translatable("chat_screen.message", string));
        }

    }

    public void renderChatPreview(PoseStack poseStack) {
        int i = (int)(255.0 * ((Double)this.minecraft.options.chatOpacity().get() * 0.8999999761581421 + 0.10000000149011612));
        int j = (int)(255.0 * (Double)this.minecraft.options.textBackgroundOpacity().get());
        int k = this.chatPreviewWidth();
        List<FormattedCharSequence> list = this.peekChatPreview();
        int l = this.chatPreviewHeight(list);
        RenderSystem.enableBlend();
        poseStack.pushPose();
        poseStack.translate((double)this.chatPreviewLeft(), (double)this.chatPreviewTop(l), 0.0);
        fill(poseStack, 0, 0, k, l, j << 24);
        poseStack.translate(2.0, 2.0, 0.0);

        for(int m = 0; m < list.size(); ++m) {
            FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(m);
            Font var10000 = this.minecraft.font;
            Objects.requireNonNull(this.font);
            var10000.drawShadow(poseStack, formattedCharSequence, 0.0F, (float)(m * 9), i << 24 | 16777215);
        }

        poseStack.popPose();
        RenderSystem.disableBlend();
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
    private Style getChatPreviewStyleAt(double d, double e) {
        if (this.minecraft.options.hideGui) {
            return null;
        } else {
            List<FormattedCharSequence> list = this.peekChatPreview();
            int i = this.chatPreviewHeight(list);
            if (!(d < (double)this.chatPreviewLeft()) && !(d > (double)this.chatPreviewRight()) && !(e < (double)this.chatPreviewTop(i)) && !(e > (double)this.chatPreviewBottom())) {
                int j = this.chatPreviewLeft() + PREVIEW_PADDING;
                int k = this.chatPreviewTop(i) + PREVIEW_PADDING;
                int var10000 = Mth.floor(e) - k;
                Objects.requireNonNull(this.font);
                int l = var10000 / 9;
                if (l >= 0 && l < list.size()) {
                    FormattedCharSequence formattedCharSequence = (FormattedCharSequence)list.get(l);
                    return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedCharSequence, (int)(d - (double)j));
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private List<FormattedCharSequence> peekChatPreview() {
        Component component = this.chatPreview.peek();
        return component != null ? this.font.split(component, this.chatPreviewWidth()) : List.of(PREVIEW_HINT.getVisualOrderText());
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

    public void handleChatInput(String string, boolean bl) {
        string = this.normalizeChatMessage(string);
        if (!string.isEmpty()) {
            if (bl) {
                this.minecraft.gui.getChat().addRecentChat(string);
            }

            Component component = this.chatPreview.pull(string);
            if (string.startsWith("/")) {
                this.minecraft.player.command(string.substring(1), component);
            } else {
                this.minecraft.player.chat(string, component);
            }

        }
    }

    public String normalizeChatMessage(String message) {
        return StringUtils.normalizeSpace(message.trim());
    }

    public ClientChatPreview getChatPreview() {
        return this.chatPreview;
    }

    static {
        PREVIEW_HINT = Component.translatable("chat.preview").withStyle(ChatFormatting.DARK_GRAY);
    }}