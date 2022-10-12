package calebzhou.rdi.core.client.screen

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.SharedConstants
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.components.Widget
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.Mth
import java.util.*
import java.util.function.BiFunction
import java.util.function.Consumer
import java.util.function.Predicate

@Environment(EnvType.CLIENT)
open class RdiChatEditBox(font: Font, x: Int, y: Int, width: Int, height: Int, component: Component) : EditBox(font,x,y,width,height,component), Widget, GuiEventListener {
    private val font: Font
    private var value = ""
    private var maxLength = 32
    private var frame = 0
    private var bordered = true
    private var canLoseFocus = true
    private var isEditable = true
    private var shiftPressed = false
    private var displayPos = 0
    private var cursorPos = 0
    private var highlightPos = 0
    private var textColor: Int
    private var textColorUneditable: Int
    private var suggestion: String? = null
    private var responder: Consumer<String>? = null
    private var filter: Predicate<String>
    private var formatter: BiFunction<String, Int, FormattedCharSequence>

    init {
        this.textColor = DEFAULT_TEXT_COLOR
        this.textColorUneditable = 0x707070
        this.filter = Predicate { obj: String? -> Objects.nonNull(obj) }
        this.formatter =
            BiFunction { string: String?, integer: Int? -> FormattedCharSequence.forward(string, Style.EMPTY) }
        this.font = font
    }

    override fun setResponder(responder: Consumer<String>) {
        this.responder = responder
    }

    override fun setFormatter(textFormatter: BiFunction<String, Int, FormattedCharSequence>) {
        this.formatter = textFormatter
    }

    override fun tick() {
        ++this.frame
    }

    override fun createNarrationMessage(): MutableComponent {
        val component = message
        return Component.translatable("gui.narrate.editBox", component, this.value)
    }

    override fun setValue(text: String) {
        if (this.filter.test(text)) {
            if (text.length > this.maxLength) {
                this.value = text.substring(0, this.maxLength)
            } else {
                this.value = text
            }
            moveCursorToEnd()
            setHighlightPos(this.cursorPos)
            onValueChange(text)
        }
    }

    override fun getValue(): String {
        return this.value
    }

    override fun getHighlighted(): String {
        val i = Math.min(this.cursorPos, this.highlightPos)
        val j = Math.max(this.cursorPos, this.highlightPos)
        return this.value.substring(i, j)
    }

    override fun setFilter(predicate: Predicate<String>) {
        this.filter = predicate
    }

    override fun insertText(textToWrite: String) {
        val i = Math.min(this.cursorPos, this.highlightPos)
        val j = Math.max(this.cursorPos, this.highlightPos)
        val k = this.maxLength - this.value.length - (i - j)
        var string = SharedConstants.filterText(textToWrite)
        var l = string.length
        if (k < l) {
            string = string.substring(0, k)
            l = k
        }
        val string2 = StringBuilder(this.value).replace(i, j, string).toString()
        if (this.filter.test(string2)) {
            this.value = string2
            this.cursorPosition = i + l
            setHighlightPos(this.cursorPos)
            onValueChange(this.value)
        }
    }

    private fun onValueChange(newText: String) {
        if (this.responder != null) {
            this.responder!!.accept(newText)
        }
    }

    private fun deleteText(count: Int) {
        if (Screen.hasControlDown()) {
            deleteWords(count)
        } else {
            deleteChars(count)
        }
    }

    /**
     * Deletes the given number of words from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    override fun deleteWords(num: Int) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                insertText("")
            } else {
                deleteChars(this.getWordPosition(num) - this.cursorPos)
            }
        }
    }

    /**
     * Deletes the given number of characters from the current cursor's position, unless there is currently a selection, in which case the selection is deleted instead.
     */
    override fun deleteChars(num: Int) {
        if (!this.value.isEmpty()) {
            if (this.highlightPos != this.cursorPos) {
                insertText("")
            } else {
                val i = getCursorPos(num)
                val j = Math.min(i, this.cursorPos)
                val k = Math.max(i, this.cursorPos)
                if (j != k) {
                    val string = StringBuilder(this.value).delete(j, k).toString()
                    if (this.filter.test(string)) {
                        this.value = string
                        moveCursorTo(j)
                    }
                }
            }
        }
    }

    /**
     * Gets the starting index of the word at the specified number of words away from the cursor position.
     */
    override fun getWordPosition(numWords: Int): Int {
        return this.getWordPosition(numWords, this.cursorPosition)
    }

    /**
     * Gets the starting index of the word at a distance of the specified number of words away from the given position.
     */
    private fun getWordPosition(n: Int, pos: Int): Int {
        return this.getWordPosition(n, pos, true)
    }

    /**
     * Like getNthWordFromPos (which wraps this), but adds option for skipping consecutive spaces
     */
    private fun getWordPosition(n: Int, pos: Int, skipWs: Boolean): Int {
        var i = pos
        val bl = n < 0
        val j = Math.abs(n)
        for (k in 0 until j) {
            if (!bl) {
                val l = this.value.length
                i = this.value.indexOf(32.toChar(), i)
                if (i == -1) {
                    i = l
                } else {
                    while (skipWs && i < l && this.value[i] == ' ') {
                        ++i
                    }
                }
            } else {
                while (skipWs && i > 0 && this.value[i - 1] == ' ') {
                    --i
                }
                while (i > 0 && this.value[i - 1] != ' ') {
                    --i
                }
            }
        }
        return i
    }

    /**
     * Moves the text cursor by a specified number of characters and clears the selection
     */
    override fun moveCursor(delta: Int) {
        moveCursorTo(getCursorPos(delta))
    }

    private fun getCursorPos(delta: Int): Int {
        return Util.offsetByCodepoints(this.value, this.cursorPos, delta)
    }

    /**
     * Sets the current position of the cursor.
     */
    override fun moveCursorTo(pos: Int) {
        this.cursorPosition = pos
        if (!this.shiftPressed) {
            setHighlightPos(this.cursorPos)
        }
        onValueChange(this.value)
    }

    override fun setCursorPosition(pos: Int) {
        this.cursorPos = Mth.clamp(pos, 0, this.value.length)
    }

    /**
     * Moves the cursor to the very start of this text box.
     */
    override fun moveCursorToStart() {
        moveCursorTo(0)
    }

    /**
     * Moves the cursor to the very end of this text box.
     */
    override fun moveCursorToEnd() {
        moveCursorTo(this.value.length)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (!canConsumeInput()) {
            false
        } else {
            this.shiftPressed = Screen.hasShiftDown()
            if (Screen.isSelectAll(keyCode)) {
                moveCursorToEnd()
                setHighlightPos(0)
                true
            } else if (Screen.isCopy(keyCode)) {
                Minecraft.getInstance().keyboardHandler.clipboard = this.highlighted
                true
            } else if (Screen.isPaste(keyCode)) {
                if (this.isEditable) {
                    insertText(Minecraft.getInstance().keyboardHandler.clipboard)
                }
                true
            } else if (Screen.isCut(keyCode)) {
                Minecraft.getInstance().keyboardHandler.clipboard = this.highlighted
                if (this.isEditable) {
                    insertText("")
                }
                true
            } else {
                when (keyCode) {
                    259 -> {
                        if (this.isEditable) {
                            this.shiftPressed = false
                            deleteText(-1)
                            this.shiftPressed = Screen.hasShiftDown()
                        }
                        true
                    }
                    260, 264, 265, 266, 267 -> false
                    261 -> {
                        if (this.isEditable) {
                            this.shiftPressed = false
                            deleteText(1)
                            this.shiftPressed = Screen.hasShiftDown()
                        }
                        true
                    }
                    262 -> {
                        if (Screen.hasControlDown()) {
                            moveCursorTo(this.getWordPosition(1))
                        } else {
                            moveCursor(1)
                        }
                        true
                    }
                    263 -> {
                        if (Screen.hasControlDown()) {
                            moveCursorTo(this.getWordPosition(-1))
                        } else {
                            moveCursor(-1)
                        }
                        true
                    }
                    268 -> {
                        moveCursorToStart()
                        true
                    }
                    269 -> {
                        moveCursorToEnd()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    override fun canConsumeInput(): Boolean {
        return this.isVisible && this.isFocused && isEditable()
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        return if (!canConsumeInput()) {
            false
        } else if (SharedConstants.isAllowedChatCharacter(codePoint)) {
            if (this.isEditable) {
                insertText(Character.toString(codePoint))
            }
            true
        } else {
            false
        }
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (!this.isVisible) {
            false
        } else {
            val bl =
                mouseX >= x.toDouble() && mouseX < (x + width).toDouble() && mouseY >= y.toDouble() && mouseY < (y + height).toDouble()
            if (this.canLoseFocus) {
                setFocus(bl)
            }
            if (this.isFocused && bl && button == 0) {
                var i = Mth.floor(mouseX) - x
                if (this.bordered) {
                    i -= 4
                }
                val string = this.font!!.plainSubstrByWidth(this.value.substring(this.displayPos), this.innerWidth)
                moveCursorTo(this.font.plainSubstrByWidth(string, i).length + this.displayPos)
                true
            } else {
                false
            }
        }
    }

    /**
     * Sets focus to this gui element
     */
    override fun setFocus(isFocused: Boolean) {
        this.isFocused = isFocused
    }

    override fun renderButton(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        if (this.isVisible) {
            var i: Int
            if (isBordered()) {
                i = if (this.isFocused) -1 else -6250336
                fill(poseStack, x - 1, y - 1, x + width + 1, y + height + 1, i)
                fill(poseStack, x, y, x + width, y + height, -16777216)
            }
            i = if (this.isEditable) this.textColor else this.textColorUneditable
            val j = this.cursorPos - this.displayPos
            var k = this.highlightPos - this.displayPos
            val string = this.font!!.plainSubstrByWidth(this.value.substring(this.displayPos), this.innerWidth)
            val bl = j >= 0 && j <= string.length
            val bl2 = this.isFocused && this.frame / 6 % 2 == 0 && bl
            val l = if (this.bordered) x + 4 else x
            val m = if (this.bordered) y + (height - 8) / 2 else y
            var n = l
            if (k > string.length) {
                k = string.length
            }
            if (!string.isEmpty()) {
                val string2 = if (bl) string.substring(0, j) else string
                val sequence = this.formatter.apply(string2, this.displayPos)
                if (sequence != null) n = this.font.drawShadow(poseStack, sequence, l.toFloat(), m.toFloat(), i)
            }
            val bl3 = this.cursorPos < this.value.length || this.value.length >= getMaxLength()
            var o = n
            if (!bl) {
                o = if (j > 0) l + width else l
            } else if (bl3) {
                o = n - 1
                --n
            }
            if (!string.isEmpty() && bl && j < string.length) {
                this.font.drawShadow(
                    poseStack,
                    this.formatter.apply(string.substring(j), this.cursorPos),
                    n.toFloat(),
                    m.toFloat(),
                    i
                )
            }
            if (!bl3 && this.suggestion != null) {
                this.font.drawShadow(poseStack, this.suggestion, (o - 1).toFloat(), m.toFloat(), -8355712)
            }
            var var10002: Int
            var var10003: Int
            var var10004: Int
            if (bl2) {
                if (bl3) {
                    var10002 = m - 1
                    var10003 = o + 1
                    var10004 = m + 1
                    Objects.requireNonNull(this.font)
                    fill(poseStack, o, var10002, var10003, var10004 + 9, -3092272)
                } else {
                    this.font.drawShadow(poseStack, "|", o.toFloat(), m.toFloat(), i)
                }
            }
            if (k != j) {
                val p = l + this.font.width(string.substring(0, k))
                var10002 = m - 1
                var10003 = p - 1
                var10004 = m + 1
                Objects.requireNonNull(this.font)
                renderHighlight(o, var10002, var10003, var10004 + 9)
            }
        }
    }

    /**
     * Draws the blue selection box.
     */
    private fun renderHighlight(startX: Int, startY: Int, endX: Int, endY: Int) {
        var startX = startX
        var startY = startY
        var endX = endX
        var endY = endY
        var i: Int
        if (startX < endX) {
            i = startX
            startX = endX
            endX = i
        }
        if (startY < endY) {
            i = startY
            startY = endY
            endY = i
        }
        if (endX > x + width) {
            endX = x + width
        }
        if (startX > x + width) {
            startX = x + width
        }
        val tesselator = Tesselator.getInstance()
        val bufferBuilder = tesselator.builder
        RenderSystem.setShader { GameRenderer.getPositionShader() }
        RenderSystem.setShaderColor(0.0f, 0.0f, 1.0f, 1.0f)
        RenderSystem.disableTexture()
        RenderSystem.enableColorLogicOp()
        RenderSystem.logicOp(GlStateManager.LogicOp.OR_REVERSE)
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION)
        bufferBuilder.vertex(startX.toDouble(), endY.toDouble(), 0.0).endVertex()
        bufferBuilder.vertex(endX.toDouble(), endY.toDouble(), 0.0).endVertex()
        bufferBuilder.vertex(endX.toDouble(), startY.toDouble(), 0.0).endVertex()
        bufferBuilder.vertex(startX.toDouble(), startY.toDouble(), 0.0).endVertex()
        tesselator.end()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        RenderSystem.disableColorLogicOp()
        RenderSystem.enableTexture()
    }

    /**
     * Sets the maximum length for the text in this text box. If the current text is longer than this length, the current text will be trimmed.
     */
    override fun setMaxLength(length: Int) {
        this.maxLength = length
        if (this.value.length > length) {
            this.value = this.value.substring(0, length)
            onValueChange(this.value)
        }
    }

    /**
     * returns the maximum number of character that can be contained in this textbox
     */
    private fun getMaxLength(): Int {
        return this.maxLength
    }

    /**
     * returns the current position of the cursor
     */
    override fun getCursorPosition(): Int {
        return this.cursorPos
    }

    /**
     * Gets whether the background and outline of this text box should be drawn (true if so).
     */
    private fun isBordered(): Boolean {
        return this.bordered
    }

    /**
     * Sets whether or not the background and outline of this text box should be drawn.
     */
    override fun setBordered(enableBackgroundDrawing: Boolean) {
        this.bordered = enableBackgroundDrawing
    }

    /**
     * Sets the color to use when drawing this text box's text. A different color is used if this text box is disabled.
     */
    override fun setTextColor(color: Int) {
        this.textColor = color
    }

    /**
     * Sets the color to use for text in this text box when this text box is disabled.
     */
    override fun setTextColorUneditable(color: Int) {
        this.textColorUneditable = color
    }

    override fun changeFocus(focus: Boolean): Boolean {
        return if (visible && this.isEditable) super<EditBox>.changeFocus(focus) else false
    }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return visible && mouseX >= x.toDouble() && mouseX < (x + width).toDouble() && mouseY >= y.toDouble() && mouseY < (y + height).toDouble()
    }

    override fun onFocusedChanged(focused: Boolean) {
        if (focused) {
            this.frame = 0
        }
    }

    private fun isEditable(): Boolean {
        return this.isEditable
    }

    /**
     * Sets whether this text box is enabled. Disabled text boxes cannot be typed in.
     */
    override fun setEditable(enabled: Boolean) {
        this.isEditable = enabled
    }

    /**
     * returns the width of the textbox depending on if background drawing is enabled
     */
    override fun getInnerWidth(): Int {
        return if (isBordered()) width - 8 else width
    }

    /**
     * Sets the position of the selection anchor (the selection anchor and the cursor position mark the edges of the selection). If the anchor is set beyond the bounds of the current text, it will be put back inside.
     */
    override fun setHighlightPos(position: Int) {
        val i = this.value.length
        this.highlightPos = Mth.clamp(position, 0, i)
        if (this.font != null) {
            if (this.displayPos > i) {
                this.displayPos = i
            }
            val j = this.innerWidth
            val string = this.font.plainSubstrByWidth(this.value.substring(this.displayPos), j)
            val k = string.length + this.displayPos
            if (this.highlightPos == this.displayPos) {
                this.displayPos -= this.font.plainSubstrByWidth(this.value, j, true).length
            }
            if (this.highlightPos > k) {
                this.displayPos += this.highlightPos - k
            } else if (this.highlightPos <= this.displayPos) {
                this.displayPos -= this.displayPos - this.highlightPos
            }
            this.displayPos = Mth.clamp(this.displayPos, 0, i)
        }
    }

    /**
     * Sets whether this text box loses focus when something other than it is clicked.
     */


    /**
     * returns true if this textbox is visible
     */
    override fun isVisible(): Boolean {
        return visible
    }

    /**
     * Sets whether or not this textbox is visible
     */

    override fun getScreenX(charNum: Int): Int {
        return if (charNum > this.value.length) x else x + this.font!!.width(this.value.substring(0, charNum))
    }


    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
        narrationElementOutput.add(
            NarratedElementType.TITLE,
            Component.translatable("narration.edit_box", this.getValue()) as Component
        )
    }

    companion object {
        const val BACKWARDS = -1
        const val FORWARDS = 1
        private const val CURSOR_INSERT_WIDTH = 1
        private const val CURSOR_INSERT_COLOR = -3092272
        private const val CURSOR_APPEND_CHARACTER = "_"
        const val DEFAULT_TEXT_COLOR = 14737632
        private const val BORDER_COLOR_FOCUSED = -1
        private const val BORDER_COLOR = -6250336
        private const val BACKGROUND_COLOR = -16777216
    }
}
