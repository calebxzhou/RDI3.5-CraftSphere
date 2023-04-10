package calebxzhou.libertorch.mc.gui.components

import calebxzhou.libertorch.mc.gui.LtTheme
import calebxzhou.libertorch.mc.mixin.AccessAbstractWidget
import calebxzhou.rdi.util.McGl
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.Button.OnTooltip
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.ComponentUtils
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.util.Mth

/**
 * Created  on 2023-02-26,21:27.
 */
class LtTextButton(x: Int, y: Int, message: Component, onPress: OnPress, onTooltip: OnTooltip, )
    : Button(x, y, message.string.length * 4, 8, message, onPress,onTooltip) {

    override fun updateNarration(narrationElementOutput: NarrationElementOutput) {
    }

    override fun renderButton(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        //如果鼠标悬浮在控件上面，就画一个色的框
        if (this.isHoveredOrFocused) {
            McGl.rect(poseStack, x, y, width, height, 1, LtTheme.now.widgetOutlineColor);
            renderToolTip(poseStack, mouseX, mouseY);
        }
        val fontColor = if (active) LtTheme.now.fontActiveColor else LtTheme.now.fontInactiveColor

        drawString(poseStack,
            Minecraft.getInstance().font,
            message,
            x,y, fontColor.hex or (Mth.ceil(alpha * 255.0f) shl 24)
        )
    }

    override fun renderToolTip(poseStack: PoseStack, relativeMouseX: Int, relativeMouseY: Int) {
        this.onTooltip.onTooltip(this,poseStack,relativeMouseX,relativeMouseY)
    }
}
