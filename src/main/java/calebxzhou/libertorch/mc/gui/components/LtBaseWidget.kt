package calebxzhou.libertorch.mc.gui.components

import calebxzhou.libertorch.mc.gui.LtTheme
import calebxzhou.libertorch.mc.mixin.AccessAbstractWidget
import calebxzhou.rdi.util.FontUt
import calebxzhou.rdi.util.McGl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.util.Mth

/**
 * Created  on 2023-04-10,19:44.
 */
open class LtBaseWidget {
    companion object{
        @JvmStatic
        fun renderButton(
            poseStack: PoseStack,
            button: AbstractWidget,
            alpha: Float,
            doAfterFill: Runnable
        ) {
            val minecraft = Minecraft.getInstance()
            val font = minecraft.font
            val x1 = button.x
            val y1 = button.y
            val width = button.width
            val height = button.height
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            //如果鼠标悬浮在控件上面，就画一个色的框
            if ((button as AccessAbstractWidget).isHovered) {
                McGl.rect(poseStack, x1, y1, width, height, 1, LtTheme.now.widgetOutlineColor);
            }

            doAfterFill.run()
            val fontColor = if (button.active) LtTheme.now.fontActiveColor else LtTheme.now.fontInactiveColor
            FontUt.drawCenteredString(
                poseStack,
                font,
                button.message,
                x1 + width / 2,
                y1 + (height - 8) / 2,
                fontColor.hex or (Mth.ceil(alpha * 255.0f) shl 24)
            )

        }
    }
}
