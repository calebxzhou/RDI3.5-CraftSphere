package calebxzhou.libertorch.mc.gui

import calebxzhou.libertorch.DefaultColorPalette
import calebxzhou.libertorch.mc.mixin.AccessAbstractWidget
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent.*
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.util.Mth

/**
 * Created  on 2023-03-12,13:02.
 */
object LTButtonRenderer {

    @JvmStatic
    fun render(
        poseStack: PoseStack,
        button: AbstractWidget,
        alpha: Float,
        doAfterFill: Runnable
    ){
        val minecraft = Minecraft.getInstance()
        val font = minecraft.font
        val x1 = button.x
        val y1 = button.y
        val width = button.width
        val height = button.height
        val x2 = x1+width
        val y2 = y1+height
        val message = button.message
        val isActive = button.active
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        //如果鼠标悬浮在控件上面，就画一个白色的框
        if((button as AccessAbstractWidget).isHovered){
            fill(poseStack,x1-1,y1-1,x2+1,y2+1,DefaultColorPalette.WHITE.opaque())
        }
        fill(poseStack, x1,y1,x2,y2, DefaultColorPalette.OLIVE_GREEN.opaque())

        doAfterFill.run()
        val j = if (isActive) 0xFFFFFF else 0xA0A0A0
        drawCenteredString(
            poseStack,
            font,
            message,
            x1 + width / 2,
            y1 + (height - 8) / 2,
            j or (Mth.ceil(alpha * 255.0f) shl 24)
        )

    }
}
