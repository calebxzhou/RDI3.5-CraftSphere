package calebzhou.rdi.craftsphere.screen;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ChatEditBox extends AbstractWidget implements Widget, GuiEventListener {
    public static final int BACKWARDS = -1;
    public static final int FORWARDS = 1;
    private static final int CURSOR_INSERT_WIDTH = 1;
    private static final int CURSOR_INSERT_COLOR = -3092272;
    private static final String CURSOR_APPEND_CHARACTER = "_";
    public static final int DEFAULT_TEXT_COLOR = 14737632;
    private static final int BORDER_COLOR_FOCUSED = -1;
    private static final int BORDER_COLOR = -6250336;
    private static final int BACKGROUND_COLOR = -16777216;
    private final Font font;
    private String value;
    private int maxLength;
    private int frame;
    private boolean bordered;
    private boolean canLoseFocus;
    private boolean isEditable;
    private boolean shiftPressed;
    private int displayPos;
    private int cursorPos;
    private int highlightPos;
    private int textColor;
    private int textColorUneditable;
    @Nullable
    private String suggestion;
    @Nullable
    private Consumer<String> responder;
    private Predicate<String> filter;
    private BiFunction<String, Integer, FormattedCharSequence> formatter;

    public ChatEditBox(Font font, int i, int j, int k, int l, Component component) {
        this(font, i, j, k, l, (EditBox)null, component);
    }

    public ChatEditBox(Font font, int i, int j, int k, int l, @Nullable EditBox editBox, Component component) {
        super(i, j, k, l, component);
        this.value = "";
        this.maxLength = 32;
        this.bordered = true;
        this.canLoseFocus = true;
        this.isEditable = true;
        this.textColor = 14737632;
        this.textColorUneditable = 7368816;
        this.filter = Objects::nonNull;
        this.formatter = (string, integer) -> {
            return FormattedCharSequence.forward(string, Style.EMPTY);
        };
        this.font = font;
        if (editBox != null) {
            this.setValue(editBox.getValue());
        }

    }

    public void setResponder(Consumer<String> consumer) {
        this.responder = consumer;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> biFunction) {
        this.formatter = biFunction;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return new TranslatableComponent("gui.narrate.editBox", new Object[]{component, this.value});
    }

    public void setValue(String string) {
        if (this.filter.test(string)) {
            if (string.length() > this.maxLength) {
                this.value = string.substring(0, this.maxLength);
            } else {
                this.value = string;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(string);
        }
    }

    public String getValue() {
        return this.value;
    }

    public String getHighlighted() {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        return this.value.substring(i, j);
    }

    public void setFilter(Predicate<String> predicate) {
        this.filter = predicate;
    }

    public void insertText(String string) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        String string2 = SharedConstants.filterText(string);
        int l = string2.length();
        if (k < l) {
            string2 = string2.substring(0, k);
            l = k;
        }

        String string3 = (new StringBuilder(this.value)).replace(i, j, string2).toString();
        if (this.filter.test(string3)) {
            this.value = string3;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String string) {
        if (this.responder != null) {
            this.responder.accept(string);
        }

    }

    private void deleteText(int i) {
        if (Screen.hasControlDown()) {
            this.deleteWords(i);
        } else {
            this.deleteChars(i);
        }

    }

    public void deleteWords(int i) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(i) - this.cursorPos);
            }
        }
    }

    public void deleteChars(int i) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int j = this.getCursorPos(i);
                int k = Math.min(j, this.cursorPos);
                int l = Math.max(j, this.cursorPos);
                if (k != l) {
                    String string = (new StringBuilder(this.value)).delete(k, l).toString();
                    if (this.filter.test(string)) {
                        this.value = string;
                        this.moveCursorTo(k);
                    }
                }
            }
        }
    }

    public int getWordPosition(int i) {
        return this.getWordPosition(i, this.getCursorPosition());
    }

    private int getWordPosition(int i, int j) {
        return this.getWordPosition(i, j, true);
    }

    private int getWordPosition(int i, int j, boolean bl) {
        int k = j;
        boolean bl2 = i < 0;
        int l = Math.abs(i);

        for(int m = 0; m < l; ++m) {
            if (!bl2) {
                int n = this.value.length();
                k = this.value.indexOf(32, k);
                if (k == -1) {
                    k = n;
                } else {
                    while(bl && k < n && this.value.charAt(k) == ' ') {
                        ++k;
                    }
                }
            } else {
                while(bl && k > 0 && this.value.charAt(k - 1) == ' ') {
                    --k;
                }

                while(k > 0 && this.value.charAt(k - 1) != ' ') {
                    --k;
                }
            }
        }

        return k;
    }

    public void moveCursor(int i) {
        this.moveCursorTo(this.getCursorPos(i));
    }

    private int getCursorPos(int i) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, i);
    }

    public void moveCursorTo(int i) {
        this.setCursorPosition(i);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int i) {
        this.cursorPos = Mth.clamp((int)i, (int)0, (int)this.value.length());
    }

    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(int i, int j, int k) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(i)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(i)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(i)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(i)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch(i) {
                    case 259:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(-1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 260:
                    case 264:
                    case 265:
                    case 266:
                    case 267:
                    default:
                        return false;
                    case 261:
                        if (this.isEditable) {
                            this.shiftPressed = false;
                            this.deleteText(1);
                            this.shiftPressed = Screen.hasShiftDown();
                        }

                        return true;
                    case 262:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(1));
                        } else {
                            this.moveCursor(1);
                        }

                        return true;
                    case 263:
                        if (Screen.hasControlDown()) {
                            this.moveCursorTo(this.getWordPosition(-1));
                        } else {
                            this.moveCursor(-1);
                        }

                        return true;
                    case 268:
                        this.moveCursorToStart();
                        return true;
                    case 269:
                        this.moveCursorToEnd();
                        return true;
                }
            }
        }
    }

    public boolean canConsumeInput() {
        return this.isVisible() && this.isFocused() && this.isEditable();
    }

    public boolean charTyped(char c, int i) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(c)) {
            if (this.isEditable) {
                this.insertText(Character.toString(c));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double d, double e, int i) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean bl = d >= (double)this.x && d < (double)(this.x + this.width) && e >= (double)this.y && e < (double)(this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(bl);
            }

            if (this.isFocused() && bl && i == 0) {
                int j = Mth.floor(d) - this.x;
                if (this.bordered) {
                    j -= 4;
                }

                String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(string, j).length() + this.displayPos);
                return true;
            } else {
                return false;
            }
        }
    }

    public void setFocus(boolean bl) {
        this.setFocused(bl);
    }

    public void renderButton(PoseStack poseStack, int i, int j, float f) {
        if (this.isVisible()) {
            int k;
            if (this.isBordered()) {
                k = this.isFocused() ? -1 : 6250336;
                fill(poseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, k);
                fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, 16777216);
            }

            k = this.isEditable ? this.textColor : this.textColorUneditable;
            int l = this.cursorPos - this.displayPos;
            int m = this.highlightPos - this.displayPos;
            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean bl = l >= 0 && l <= string.length();
            boolean bl2 = this.isFocused() && this.frame / 6 % 2 == 0 && bl;
            int n = this.bordered ? this.x + 4 : this.x;
            int o = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int p = n;
            if (m > string.length()) {
                m = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, l) : string;
                p = this.font.drawShadow(poseStack, (FormattedCharSequence)this.formatter.apply(string2, this.displayPos), (float)n, (float)o, k);
            }

            boolean bl3 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int q = p;
            if (!bl) {
                q = l > 0 ? n + this.width : n;
            } else if (bl3) {
                q = p - 1;
                --p;
            }

            if (!string.isEmpty() && bl && l < string.length()) {
                this.font.drawShadow(poseStack, (FormattedCharSequence)this.formatter.apply(string.substring(l), this.cursorPos), (float)p, (float)o, k);
            }

            if (!bl3 && this.suggestion != null) {
                this.font.drawShadow(poseStack, this.suggestion, (float)(q - 1), (float)o, 0x00808080);
            }

            int var10002;
            int var10003;
            int var10004;
            if (bl2) {
                if (bl3) {
                    var10002 = o - 1;
                    var10003 = q + 1;
                    var10004 = o + 1;
                    Objects.requireNonNull(this.font);
                    GuiComponent.fill(poseStack, q, var10002, var10003, var10004 + 9, 0x00000000);
                } else {
                    this.font.drawShadow(poseStack, "_", (float)q, (float)o, k);
                }
            }

            if (m != l) {
                int r = n + this.font.width(string.substring(0, m));
                var10002 = o - 1;
                var10003 = r - 1;
                var10004 = o + 1;
                Objects.requireNonNull(this.font);
                this.renderHighlight(q, var10002, var10003, var10004 + 9);
            }

        }
    }

    private void renderHighlight(int i, int j, int k, int l) {
        int m;
        if (i < k) {
            m = i;
            i = k;
            k = m;
        }

        if (j < l) {
            m = j;
            j = l;
            l = m;
        }

        if (k > this.x + this.width) {
            k = this.x + this.width;
        }

        if (i > this.x + this.width) {
            i = this.x + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex((double)i, (double)l, 0.0D).endVertex();
        bufferBuilder.vertex((double)k, (double)l, 0.0D).endVertex();
        bufferBuilder.vertex((double)k, (double)j, 0.0D).endVertex();
        bufferBuilder.vertex((double)i, (double)j, 0.0D).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    public void setMaxLength(int i) {
        this.maxLength = i;
        if (this.value.length() > i) {
            this.value = this.value.substring(0, i);
            this.onValueChange(this.value);
        }

    }

    private int getMaxLength() {
        return this.maxLength;
    }

    public int getCursorPosition() {
        return this.cursorPos;
    }

    private boolean isBordered() {
        return this.bordered;
    }

    public void setBordered(boolean bl) {
        this.bordered = bl;
    }

    public void setTextColor(int i) {
        this.textColor = i;
    }

    public void setTextColorUneditable(int i) {
        this.textColorUneditable = i;
    }

    public boolean changeFocus(boolean bl) {
        return this.visible && this.isEditable ? super.changeFocus(bl) : false;
    }

    public boolean isMouseOver(double d, double e) {
        return this.visible && d >= (double)this.x && d < (double)(this.x + this.width) && e >= (double)this.y && e < (double)(this.y + this.height);
    }

    protected void onFocusedChanged(boolean bl) {
        if (bl) {
            this.frame = 0;
        }

    }

    private boolean isEditable() {
        return this.isEditable;
    }

    public void setEditable(boolean bl) {
        this.isEditable = bl;
    }

    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    public void setHighlightPos(int i) {
        int j = this.value.length();
        this.highlightPos = Mth.clamp((int)i, (int)0, (int)j);
        if (this.font != null) {
            if (this.displayPos > j) {
                this.displayPos = j;
            }

            int k = this.getInnerWidth();
            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), k);
            int l = string.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, k, true).length();
            }

            if (this.highlightPos > l) {
                this.displayPos += this.highlightPos - l;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp((int)this.displayPos, (int)0, (int)j);
        }

    }

    public void setCanLoseFocus(boolean bl) {
        this.canLoseFocus = bl;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public void setVisible(boolean bl) {
        this.visible = bl;
    }

    public void setSuggestion(@Nullable String string) {
        this.suggestion = string;
    }

    public int getScreenX(int i) {
        return i > this.value.length() ? this.x : this.x + this.font.width(this.value.substring(0, i));
    }

    public void setX(int i) {
        this.x = i;
    }

    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)(new TranslatableComponent("narration.edit_box", new Object[]{this.getValue()})));
    }
}
