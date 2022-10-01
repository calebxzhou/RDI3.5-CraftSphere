package calebzhou.rdi.core.client.screen;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public class RdiChatEditBox extends EditBox implements Widget, GuiEventListener {
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

    public RdiChatEditBox(Font font, int i, int j, int k, int l, Component component) {
        this(font, i, j, k, l, null, component);
    }

    public RdiChatEditBox(Font font, int i, int j, int k, int l, @Nullable net.minecraft.client.gui.components.EditBox editBox, Component component) {
        super(font,i, j, k, l, component);
        this.value = "";
        this.maxLength = 32;
        this.bordered = true;
        this.canLoseFocus = true;
        this.isEditable = true;
        this.textColor = DEFAULT_TEXT_COLOR;
        this.textColorUneditable = 0x707070;
        this.filter = Objects::nonNull;
        this.formatter = (string, integer) -> {
            return FormattedCharSequence.forward(string, Style.EMPTY);
        };
        this.font = font;
        if (editBox != null) {
            this.setValue(editBox.getValue());
        }

    }

    public void setResponder(Consumer<String> responder) {
        this.responder = responder;
    }

    public void setFormatter(BiFunction<String, Integer, FormattedCharSequence> textFormatter) {
        this.formatter = textFormatter;
    }

    public void tick() {
        ++this.frame;
    }

    protected MutableComponent createNarrationMessage() {
        Component component = this.getMessage();
        return Component.translatable("gui.narrate.editBox", component, this.value);
    }

    public void setValue(String text) {
        if (this.filter.test(text)) {
            if (text.length() > this.maxLength) {
                this.value = text.substring(0, this.maxLength);
            } else {
                this.value = text;
            }

            this.moveCursorToEnd();
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(text);
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

    public void insertText(String textToWrite) {
        int i = Math.min(this.cursorPos, this.highlightPos);
        int j = Math.max(this.cursorPos, this.highlightPos);
        int k = this.maxLength - this.value.length() - (i - j);
        String string = SharedConstants.filterText(textToWrite);
        int l = string.length();
        if (k < l) {
            string = string.substring(0, k);
            l = k;
        }

        String string2 = (new StringBuilder(this.value)).replace(i, j, string).toString();
        if (this.filter.test(string2)) {
            this.value = string2;
            this.setCursorPosition(i + l);
            this.setHighlightPos(this.cursorPos);
            this.onValueChange(this.value);
        }
    }

    private void onValueChange(String newText) {
        if (this.responder != null) {
            this.responder.accept(newText);
        }

    }

    private void deleteText(int count) {
        if (Screen.hasControlDown()) {
            this.deleteWords(count);
        } else {
            this.deleteChars(count);
        }

    }


    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    public void deleteWords(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                this.deleteChars(this.getWordPosition(num) - this.cursorPos);
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    public void deleteChars(int num) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                this.insertText("");
            } else {
                int i = this.getCursorPos(num);
                int j = Math.min(i, this.cursorPos);
                int k = Math.max(i, this.cursorPos);
                if (j != k) {
                    String string = (new StringBuilder(this.value)).delete(j, k).toString();
                    if (this.filter.test(string)) {
                        this.value = string;
                        this.moveCursorTo(j);
                    }
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    public int getWordPosition(int numWords) {
        return this.getWordPosition(numWords, this.getCursorPosition());
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private int getWordPosition(int n, int pos) {
        return this.getWordPosition(n, pos, true);
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private int getWordPosition(int n, int pos, boolean skipWs) {
        int i = pos;
        boolean bl = n < 0;
        int j = Math.abs(n);

        for(int k = 0; k < j; ++k) {
            if (!bl) {
                int l = this.value.length();
                i = this.value.indexOf(32, i);
                if (i == -1) {
                    i = l;
                } else {
                    while(skipWs && i < l && this.value.charAt(i) == ' ') {
                        ++i;
                    }
                }
            } else {
                while(skipWs && i > 0 && this.value.charAt(i - 1) == ' ') {
                    --i;
                }

                while(i > 0 && this.value.charAt(i - 1) != ' ') {
                    --i;
                }
            }
        }

        return i;
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    public void moveCursor(int delta) {
        this.moveCursorTo(this.getCursorPos(delta));
    }

    private int getCursorPos(int delta) {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta);
    }

    /**
     * Sets the current position of the cursor.
     */
    public void moveCursorTo(int pos) {
        this.setCursorPosition(pos);
        if (!this.shiftPressed) {
            this.setHighlightPos(this.cursorPos);
        }

        this.onValueChange(this.value);
    }

    public void setCursorPosition(int pos) {
        this.cursorPos = Mth.clamp((int)pos, (int)0, (int)this.value.length());
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    public void moveCursorToStart() {
        this.moveCursorTo(0);
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    public void moveCursorToEnd() {
        this.moveCursorTo(this.value.length());
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else {
            this.shiftPressed = Screen.hasShiftDown();
            if (Screen.isSelectAll(keyCode)) {
                this.moveCursorToEnd();
                this.setHighlightPos(0);
                return true;
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                return true;
            } else if (Screen.isPaste(keyCode)) {
                if (this.isEditable) {
                    this.insertText(Minecraft.getInstance().keyboardHandler.getClipboard());
                }

                return true;
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.setClipboard(this.getHighlighted());
                if (this.isEditable) {
                    this.insertText("");
                }

                return true;
            } else {
                switch (keyCode) {
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

    public boolean charTyped(char codePoint, int modifiers) {
        if (!this.canConsumeInput()) {
            return false;
        } else if (SharedConstants.isAllowedChatCharacter(codePoint)) {
            if (this.isEditable) {
                this.insertText(Character.toString(codePoint));
            }

            return true;
        } else {
            return false;
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isVisible()) {
            return false;
        } else {
            boolean bl = mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
            if (this.canLoseFocus) {
                this.setFocus(bl);
            }

            if (this.isFocused() && bl && button == 0) {
                int i = Mth.floor(mouseX) - this.x;
                if (this.bordered) {
                    i -= 4;
                }

                String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
                this.moveCursorTo(this.font.plainSubstrByWidth(string, i).length() + this.displayPos);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Sets focus to this gui element
     */
    public void setFocus(boolean isFocused) {
        this.setFocused(isFocused);
    }

    public void renderButton(PoseStack poseStack, int mouseX, int mouseY, float partialTick) {
        if (this.isVisible()) {
            int i;
            if (this.isBordered()) {
                i = this.isFocused() ? -1 : -6250336;
                fill(poseStack, this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, i);
                fill(poseStack, this.x, this.y, this.x + this.width, this.y + this.height, -16777216);
            }

            i = this.isEditable ? this.textColor : this.textColorUneditable;
            int j = this.cursorPos - this.displayPos;
            int k = this.highlightPos - this.displayPos;
            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), this.getInnerWidth());
            boolean bl = j >= 0 && j <= string.length();
            boolean bl2 = this.isFocused() && this.frame / 6 % 2 == 0 && bl;
            int l = this.bordered ? this.x + 4 : this.x;
            int m = this.bordered ? this.y + (this.height - 8) / 2 : this.y;
            int n = l;
            if (k > string.length()) {
                k = string.length();
            }

            if (!string.isEmpty()) {
                String string2 = bl ? string.substring(0, j) : string;
                n = this.font.drawShadow(poseStack, (FormattedCharSequence)this.formatter.apply(string2, this.displayPos), (float)l, (float)m, i);
            }

            boolean bl3 = this.cursorPos < this.value.length() || this.value.length() >= this.getMaxLength();
            int o = n;
            if (!bl) {
                o = j > 0 ? l + this.width : l;
            } else if (bl3) {
                o = n - 1;
                --n;
            }

            if (!string.isEmpty() && bl && j < string.length()) {
                this.font.drawShadow(poseStack, (FormattedCharSequence)this.formatter.apply(string.substring(j), this.cursorPos), (float)n, (float)m, i);
            }

            if (!bl3 && this.suggestion != null) {
                this.font.drawShadow(poseStack, this.suggestion, (float)(o - 1), (float)m, -8355712);
            }

            int var10002;
            int var10003;
            int var10004;
            if (bl2) {
                if (bl3) {
                    var10002 = m - 1;
                    var10003 = o + 1;
                    var10004 = m + 1;
                    Objects.requireNonNull(this.font);
                    GuiComponent.fill(poseStack, o, var10002, var10003, var10004 + 9, -3092272);
                } else {
                    this.font.drawShadow(poseStack, "|", (float)o, (float)m, i);
                }
            }

            if (k != j) {
                int p = l + this.font.width(string.substring(0, k));
                var10002 = m - 1;
                var10003 = p - 1;
                var10004 = m + 1;
                Objects.requireNonNull(this.font);
                this.renderHighlight(o, var10002, var10003, var10004 + 9);
            }

        }
    }

    /**
     * Draws the blue selection box.
     */
    private void renderHighlight(int startX, int startY, int endX, int endY) {
        int i;
        if (startX < endX) {
            i = startX;
            startX = endX;
            endX = i;
        }

        if (startY < endY) {
            i = startY;
            startY = endY;
            endY = i;
        }

        if (endX > this.x + this.width) {
            endX = this.x + this.width;
        }

        if (startX > this.x + this.width) {
            startX = this.x + this.width;
        }

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0.0F, 0.0F, 1.0F, 1.0F);
        RenderSystem.disableTexture();
        RenderSystem.enableColorLogicOp();
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE);
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex((double)startX, (double)endY, 0.0).endVertex();
        bufferBuilder.vertex((double)endX, (double)endY, 0.0).endVertex();
        bufferBuilder.vertex((double)endX, (double)startY, 0.0).endVertex();
        bufferBuilder.vertex((double)startX, (double)startY, 0.0).endVertex();
        tesselator.end();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableColorLogicOp();
        RenderSystem.enableTexture();
    }

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the current text will be trimmed.
     */
    public void setMaxLength(int length) {
        this.maxLength = length;
        if (this.value.length() > length) {
            this.value = this.value.substring(0, length);
            this.onValueChange(this.value);
        }

    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    private int getMaxLength() {
        return this.maxLength;
    }

    /**
     * returns the current position of the cursor
     */
    public int getCursorPosition() {
        return this.cursorPos;
    }

    /**
     * Gets whether the background and outline of this text box should be drawn (true if so).
     */
    private boolean isBordered() {
        return this.bordered;
    }

    /**
     * Sets whether or not the background and outline of this text box should be drawn.
     */
    public void setBordered(boolean enableBackgroundDrawing) {
        this.bordered = enableBackgroundDrawing;
    }

    /**
     * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabled.
     */
    public void setTextColor(int color) {
        this.textColor = color;
    }

    /**
     * Sets the color to use for text in this text box when this text box is disabled.
     */
    public void setTextColorUneditable(int color) {
        this.textColorUneditable = color;
    }

    public boolean changeFocus(boolean focus) {
        return this.visible && this.isEditable ? super.changeFocus(focus) : false;
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.visible && mouseX >= (double)this.x && mouseX < (double)(this.x + this.width) && mouseY >= (double)this.y && mouseY < (double)(this.y + this.height);
    }

    protected void onFocusedChanged(boolean focused) {
        if (focused) {
            this.frame = 0;
        }

    }

    private boolean isEditable() {
        return this.isEditable;
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be typed in.
     */
    public void setEditable(boolean enabled) {
        this.isEditable = enabled;
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    public int getInnerWidth() {
        return this.isBordered() ? this.width - 8 : this.width;
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    public void setHighlightPos(int position) {
        int i = this.value.length();
        this.highlightPos = Mth.clamp((int)position, (int)0, (int)i);
        if (this.font != null) {
            if (this.displayPos > i) {
                this.displayPos = i;
            }

            int j = this.getInnerWidth();
            String string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j);
            int k = string.length() + this.displayPos;
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length();
            }

            if (this.highlightPos > k) {
                this.displayPos += this.highlightPos - k;
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos;
            }

            this.displayPos = Mth.clamp((int)this.displayPos, (int)0, (int)i);
        }

    }

    /**
     * Sets whether this text box loses focus when something other than it is clicked.
     */
    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }

    /**
     * returns true if this textbox is visible
     */
    public boolean isVisible() {
        return this.visible;
    }

    /**
     * Sets whether or not this textbox is visible
     */
    public void setVisible(boolean isVisible) {
        this.visible = isVisible;
    }

    public void setSuggestion(@Nullable String suggestion) {
        this.suggestion = suggestion;
    }

    public int getScreenX(int charNum) {
        return charNum > this.value.length() ? this.x : this.x + this.font.width(this.value.substring(0, charNum));
    }

    public void setX(int x) {
        this.x = x;
    }

    public void updateNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, (Component)Component.translatable("narration.edit_box", this.getValue()));
    }
}
