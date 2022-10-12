package calebzhou.rdi.core.client.screen

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.chat.ChatPreviewAnimator
import net.minecraft.client.gui.components.CommandSuggestions
import net.minecraft.client.gui.narration.NarratedElementType
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth
import org.apache.commons.lang3.StringUtils
import java.util.function.Consumer

class RdiChatScreen(private var initial: String) : Screen(Component.literal("")) {
    private var historyBuffer = ""
    private var historyPos = -1
    val input: RdiChatEditBox= object : RdiChatEditBox(font, 4, height - 12, width - 4, 12, Component.translatable("chat.editBox")) {
        override fun createNarrationMessage(): MutableComponent {
            return super.createNarrationMessage().append(commandSuggestions!!.narrationMessage)
        }
    }
    var commandSuggestions: CommandSuggestions? = null
    private val previewNotRequired = false
    private val chatPreviewAnimator = ChatPreviewAnimator()
    override fun init() {
        minecraft!!.keyboardHandler.setSendRepeatsToGui(true)
        historyPos = minecraft!!.gui.chat.recentChat.size
        input.setMaxLength(256)
        input.setBordered(false)
        input.value = initial
        input.setResponder(Consumer { value: String -> onEdited(value) })
        addWidget(input)
        commandSuggestions = CommandSuggestions(minecraft, this, input, font, false, false, 1, 10, true, -805306368)
        commandSuggestions!!.updateCommandInfo()
        setInitialFocus(input)
    }

    override fun resize(minecraft: Minecraft, width: Int, height: Int) {
        val string = input!!.value
        this.init(minecraft, width, height)
        setChatLine(string)
        commandSuggestions!!.updateCommandInfo()
    }

    override fun removed() {
        minecraft!!.keyboardHandler.setSendRepeatsToGui(false)
        minecraft!!.gui.chat.resetChatScroll()
    }

    override fun tick() {
        input!!.tick()
    }

    private fun onEdited(value: String) {
        val string = input!!.value
        commandSuggestions!!.setAllowSuggestions(string != initial)
        commandSuggestions!!.updateCommandInfo()
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return if (commandSuggestions!!.keyPressed(keyCode, scanCode, modifiers)) {
            true
        } else if (super.keyPressed(keyCode, scanCode, modifiers)) {
            true
        } else if (keyCode == 256) {
            minecraft!!.setScreen(null as Screen?)
            true
        } else if (keyCode != 257 && keyCode != 335) {
            if (keyCode == 265) {
                moveInHistory(-1)
                true
            } else if (keyCode == 264) {
                moveInHistory(1)
                true
            } else if (keyCode == 266) {
                minecraft!!.gui.chat.scrollChat(minecraft!!.gui.chat.linesPerPage - 1)
                true
            } else if (keyCode == 267) {
                minecraft!!.gui.chat.scrollChat(-minecraft!!.gui.chat.linesPerPage + 1)
                true
            } else {
                false
            }
        } else {
            handleChatInput(input!!.value, true)
            minecraft!!.setScreen(null as Screen?)
            true
        }
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, delta: Double): Boolean {
        var delta = delta
        delta = Mth.clamp(delta, -1.0, 1.0)
        return if (commandSuggestions!!.mouseScrolled(delta)) {
            true
        } else {
            if (!hasShiftDown()) {
                delta *= MOUSE_SCROLL_SPEED
            }
            minecraft!!.gui.chat.scrollChat(delta.toInt())
            true
        }
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        super.mouseMoved(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        return if (commandSuggestions!!.mouseClicked(mouseX.toInt().toDouble(), mouseY.toInt().toDouble(), button)) {
            true
        } else {
            if (button == 0) {
                val chatComponent = minecraft!!.gui.chat
                if (chatComponent.handleChatQueueClicked(mouseX, mouseY)) {
                    return true
                }
                val style = getComponentStyleAt(mouseX, mouseY)
                if (style != null && handleComponentClicked(style)) {
                    initial = input!!.value
                    return true
                }
            }
            input!!.mouseClicked(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button)
        }
    }

    override fun insertText(text: String, overwrite: Boolean) {
        if (overwrite) {
            input!!.value = text
        } else {
            input!!.insertText(text)
        }
    }

    /**
     * input is relative and is applied directly to the sentHistoryCursor so -1 is the previous message, 1 is the next message from the current cursor position
     */
    fun moveInHistory(msgPos: Int) {
        var i = historyPos + msgPos
        val j = minecraft!!.gui.chat.recentChat.size
        i = Mth.clamp(i, 0, j)
        if (i != historyPos) {
            if (i == j) {
                historyPos = j
                input!!.value = historyBuffer
            } else {
                if (historyPos == j) {
                    historyBuffer = input!!.value
                }
                input!!.value = minecraft!!.gui.chat.recentChat[i] as String
                commandSuggestions!!.setAllowSuggestions(false)
                historyPos = i
            }
        }
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        focused = input
        input!!.setFocus(true)
        fill(poseStack, 2, height - 14, width - 2, height - 2, minecraft!!.options.getBackgroundColor(Int.MIN_VALUE))
        input!!.render(poseStack, mouseX, mouseY, partialTick)
        super.render(poseStack, mouseX, mouseY, partialTick)
        val bl = minecraft!!.profileKeyPairManager.signer() != null
        commandSuggestions!!.render(poseStack, mouseX, mouseY)
        if (bl) {
            poseStack.pushPose()
            fill(poseStack, 0, height - 14, 2, height - 2, -8932375)
            poseStack.popPose()
        }
        val style = getComponentStyleAt(mouseX.toDouble(), mouseY.toDouble())
        if (style != null && style.hoverEvent != null) {
            renderComponentHoverEffect(poseStack, style, mouseX, mouseY)
        } else {
            val guiMessageTag = minecraft!!.gui.chat.getMessageTagAt(mouseX.toDouble(), mouseY.toDouble())
            if (guiMessageTag != null && guiMessageTag.text() != null) {
                this.renderTooltip(poseStack, font.split(guiMessageTag.text(), 260), mouseX, mouseY)
            }
        }
        super.render(poseStack, mouseX, mouseY, partialTick)
    }

    override fun isPauseScreen(): Boolean {
        return false
    }

    private fun setChatLine(chatLine: String) {
        input!!.value = chatLine
    }

    override fun updateNarrationState(output: NarrationElementOutput) {
        output.add(NarratedElementType.TITLE, getTitle())
        output.add(NarratedElementType.USAGE, USAGE_TEXT)
        val string = input!!.value
        if (!string.isEmpty()) {
            output.nest()
                .add(NarratedElementType.TITLE, Component.translatable("chat_screen.message", string) as Component)
        }
    }

    private fun getComponentStyleAt(d: Double, e: Double): Style? {
        return minecraft!!.gui.chat.getClickedComponentStyleAt(d, e)
    }

    fun handleChatInput(message: String, addToChat: Boolean): Boolean {
        var message = message
        message = normalizeChatMessage(message)
        return if (message.isEmpty()) {
            true
        } else {
            if (addToChat) {
                minecraft!!.gui.chat.addRecentChat(message)
            }
            if (message.startsWith("/")) {
                //是指令 作为指令发送
                minecraft!!.player!!.commandSigned(message.substring(1), null)
            } else {
                //不是指令 作为信息发送（/speak xxx)
                minecraft!!.player!!.commandSigned("speak $message", null)
            }
            true
        }
    }

    fun normalizeChatMessage(message: String): String {
        return StringUtils.normalizeSpace(message.trim { it <= ' ' })
    }

    companion object {
        const val MOUSE_SCROLL_SPEED = 7.0
        private val USAGE_TEXT: Component = Component.translatable("chat_screen.usage")
    }
}
