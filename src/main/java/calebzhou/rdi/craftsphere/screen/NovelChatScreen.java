package calebzhou.rdi.craftsphere.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

public class NovelChatScreen extends Screen {
    public static final int MOUSE_SCROLL_SPEED = 7;
    private static final Component USAGE_TEXT = new TranslatableComponent("chat_screen.usage");
    private String historyBuffer = "";
    private int historyPos = -1;
    protected ChatEditBox input;
    private final String initial;
   // CommandSuggestions commandSuggestions;

    public NovelChatScreen(String string) {
        super(new TranslatableComponent("chat_screen.title"));
        this.initial = string;
    }

    protected void init() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.historyPos = this.minecraft.gui.getChat().getRecentChat().size();
        this.input = new ChatEditBox(this.font, 4, this.height - 12, this.width - 4, 12, new TranslatableComponent("chat.editBox")) {
            /*protected MutableComponent createNarrationMessage() {
                return super.createNarrationMessage().append(net.minecraft.client.gui.screens.ChatScreen.this.commandSuggestions.getNarrationMessage());
            }*/
        };
        this.input.setMaxLength(256);
        this.input.setBordered(false);
        this.input.setValue(this.initial);
        this.input.setResponder(this::onEdited);
        this.addWidget(this.input);
        /*this.commandSuggestions = new CommandSuggestions(this.minecraft, this, this.input, this.font, false, false, 1, 10, true, -805306368);
       // this.commandSuggestions.updateCommandInfo();*/
        this.setInitialFocus(this.input);
    }

    public void resize(Minecraft minecraft, int i, int j) {
        String string = this.input.getValue();
        this.init(minecraft, i, j);
        this.setChatLine(string);
       //// this.commandSuggestions.updateCommandInfo();
    }

    public void removed() {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
        this.minecraft.gui.getChat().resetChatScroll();
    }

    public void tick() {
        this.input.tick();
    }

    private void onEdited(String string) {
        String string2 = this.input.getValue();
       // this.commandSuggestions.setAllowSuggestions(!string2.equals(this.initial));
       // this.commandSuggestions.updateCommandInfo();
    }

    public boolean keyPressed(int i, int j, int k) {
        /*if (this.commandSuggestions.keyPressed(i, j, k)) {
            return true;
        } else*/ if (super.keyPressed(i, j, k)) {
            return true;
        } else if (i == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(null);
            return true;
        } else if (i != GLFW.GLFW_KEY_ENTER && i != 335) {
            if (i == GLFW.GLFW_KEY_UP) {
                this.moveInHistory(-1);
                return true;
            } else if (i == GLFW.GLFW_KEY_DOWN ) {
                this.moveInHistory(1);
                return true;
            } else if (i == GLFW.GLFW_KEY_PAGE_UP) {
                this.minecraft.gui.getChat().scrollChat(this.minecraft.gui.getChat().getLinesPerPage() - 1);
                return true;
            } else if (i == GLFW.GLFW_KEY_PAGE_DOWN) {
                this.minecraft.gui.getChat().scrollChat(-this.minecraft.gui.getChat().getLinesPerPage() + 1);
                return true;
            } else {
                return false;
            }
        } else {
            String string = this.input.getValue().trim();
            if (!string.isEmpty()) {
                this.sendMessage(string);
            }

            this.minecraft.setScreen((Screen)null);
            return true;
        }
    }

    public boolean mouseScrolled(double d, double e, double f) {
        if (f > 1.0D) {
            f = 1.0D;
        }

        if (f < -1.0D) {
            f = -1.0D;
        }

        /*if (this.commandSuggestions.mouseScrolled(f)) {
            return true;
        } else */{
            if (!hasShiftDown()) {
                f *= 7.0D;
            }

            this.minecraft.gui.getChat().scrollChat(f);
            return true;
        }
    }

    public boolean mouseClicked(double d, double e, int i) {
        /*if (this.commandSuggestions.mouseClicked((double)((int)d), (double)((int)e), i)) {
            return true;
        } else*/ {
            if (i == 0) {
                ChatComponent chatComponent = this.minecraft.gui.getChat();
                if (chatComponent.handleChatQueueClicked(d, e)) {
                    return true;
                }

                Style style = chatComponent.getClickedComponentStyleAt(d, e);
                if (style != null && this.handleComponentClicked(style)) {
                    return true;
                }
            }

            return this.input.mouseClicked(d, e, i) ? true : super.mouseClicked(d, e, i);
        }
    }

    protected void insertText(String string, boolean bl) {
        if (bl) {
            this.input.setValue(string);
        } else {
            this.input.insertText(string);
        }

    }

    public void moveInHistory(int i) {
        int j = this.historyPos + i;
        int k = this.minecraft.gui.getChat().getRecentChat().size();
        j = Mth.clamp((int)j, (int)0, (int)k);
        if (j != this.historyPos) {
            if (j == k) {
                this.historyPos = k;
                this.input.setValue(this.historyBuffer);
            } else {
                if (this.historyPos == k) {
                    this.historyBuffer = this.input.getValue();
                }

                this.input.setValue((String)this.minecraft.gui.getChat().getRecentChat().get(j));
               // this.commandSuggestions.setAllowSuggestions(false);
                this.historyPos = j;
            }
        }
    }

    public void render(PoseStack poseStack, int i, int j, float f) {
        this.setFocused(this.input);
        this.input.setFocus(true);
        fill(poseStack, 2, this.height - 14, this.width - 2, this.height - 2, this.minecraft.options.getBackgroundColor(Integer.MIN_VALUE));
        this.input.render(poseStack, i, j, f);
       // this.commandSuggestions.render(poseStack, i, j);
        Style style = this.minecraft.gui.getChat().getClickedComponentStyleAt((double)i, (double)j);
        if (style != null && style.getHoverEvent() != null) {
            this.renderComponentHoverEffect(poseStack, style, i, j);
        }

        super.render(poseStack, i, j, f);
    }

    public boolean isPauseScreen() {
        return false;
    }

    private void setChatLine(String string) {
        this.input.setValue(string);
    }

    protected void updateNarrationState(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getTitle());
        narrationElementOutput.add(NarratedElementType.USAGE, USAGE_TEXT);
        String string = this.input.getValue();
        if (!string.isEmpty()) {
            narrationElementOutput.nest().add(NarratedElementType.TITLE, (Component)(new TranslatableComponent("chat_screen.message", new Object[]{string})));
        }

    }
}
