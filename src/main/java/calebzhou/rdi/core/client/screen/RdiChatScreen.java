package calebzhou.rdi.core.client.screen;

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
    private String historyBuffer = "";
    private int historyPos = -1;
    public RdiChatEditBox input;
    private String initial;
    CommandSuggestions commandSuggestions;
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
        /*EmojiClientProxy.emojiSuggestionHelper = new EmojiSuggestionHelper(this);
        EmojiClientProxy.emojiSelectionGui = new EmojiSelectionGui(this);
        EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);*/
        this.setInitialFocus(this.input);
    }

    public void resize(Minecraft minecraft, int width, int height) {
        String string = this.input.getValue();
        this.init(minecraft, width, height);
        this.setChatLine(string);
        this.commandSuggestions.updateCommandInfo();
        //EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.gui.getChat().resetChatScroll();
    }

    public void tick() {
        this.input.tick();
    }

    private void onEdited(String value) {
        String string = this.input.getValue();
        this.commandSuggestions.setAllowSuggestions(!string.equals(this.initial));
        this.commandSuggestions.updateCommandInfo();
       // EmojiClientProxy.emojiSuggestionHelper.updateSuggestionList(false);
    }


    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (this.commandSuggestions.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }/* else if (EmojiClientProxy.emojiSelectionGui.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (EmojiClientProxy.emojiSuggestionHelper.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }*/ else if (super.keyPressed(keyCode, scanCode, modifiers)) {
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
        }/* else if (EmojiClientProxy.emojiSelectionGui.mouseScrolled(mouseX, mouseY, delta)) {
            return true;
        }*/ else {
            if (!hasShiftDown()) {
                delta *= MOUSE_SCROLL_SPEED;
            }

            this.minecraft.gui.getChat().scrollChat((int) delta);
            return true;
        }
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        //EmojiClientProxy.emojiSelectionGui.mouseMoved(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.commandSuggestions.mouseClicked((double) ((int) mouseX), (double) ((int) mouseY), button)) {
            return true;
        }/* else if (EmojiClientProxy.emojiSelectionGui.mouseClicked(mouseX, mouseY))
            return true;*/
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

            this.commandSuggestions.render(poseStack, mouseX, mouseY);
           // EmojiClientProxy.emojiSuggestionHelper.render(poseStack);  this.commandSuggestions.renderSuggestions(poseStack, mouseX, mouseY);
            if (bl) {
                poseStack.pushPose();
                fill(poseStack, 0, this.height - 14, 2, this.height - 2, -8932375);
                poseStack.popPose();
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

        //EmojiClientProxy.emojiSelectionGui.render(poseStack);
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
            output.nest().add(NarratedElementType.TITLE, (Component) Component.translatable("chat_screen.message", string));
        }

    }

    @Nullable
    private Style getComponentStyleAt(double d, double e) {
        Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt(d, e);
        return style;
    }

    public boolean handleChatInput(String message, boolean addToChat) {
        message = this.normalizeChatMessage(message);
        if (message.isEmpty()) {
            return true;
        } else {
            if (addToChat) {
                this.minecraft.gui.getChat().addRecentChat(message);
            }

            //Component component = Util.mapNullable(this.chatPreview.pull(message), ClientChatPreview.Preview::response);
            if (message.startsWith("/")) {
				//是指令 作为指令发送
                this.minecraft.player.commandSigned(message.substring(1),null);
            } else {
				//不是指令 作为信息发送（/speak xxx)
                this.minecraft.player.commandSigned("speak "+message,null);
            }

            return true;
        }
    }


    public String normalizeChatMessage(String message) {
        return StringUtils.normalizeSpace(message.trim());
    }

}
