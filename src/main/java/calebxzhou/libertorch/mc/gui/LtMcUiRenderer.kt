package calebxzhou.libertorch.mc.gui

import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.mc.mixin.AccessAbstractWidget
import calebxzhou.libertorch.util.Gl.clearColor
import calebxzhou.rdi.util.FontUt
import calebxzhou.rdi.util.McGl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.util.Mth

/**
 * Created  on 2023-03-12,13:02.
 */
object LtMcUiRenderer {

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

    @JvmStatic
    fun renderSlider(
        poseStack: PoseStack,
        x: Int,
        y: Int,
        width: Int,
        height: Int,
        value: Double,
        isHover: Boolean
    ) {
        if (isHover) {
            //鼠标悬浮在滑条上面，就画一个滑块

            val xPos = (x + value * (width - 8)).toInt()
            McGl.rect(
                poseStack,
                xPos, y, 3, height, 1, LtTheme.now.sliderHandleColor
            )
        }
    }

    @JvmStatic
    fun renderBg(
        poseStack: PoseStack,
        width: Int,
        height: Int,
        isInGame: Boolean
    ) {
        if (isInGame) {
            fillGradient(poseStack, 0, 0, width, height, -0x3FEFEFF0, -0x2FEFEFF0);
        } else {
            renderBg()
        }
    }

    @JvmStatic
    fun renderBg() {
        clearColor(DefaultColors.PineGreen.color)
    }

    fun fillGradient(poseStack: PoseStack, x1: Int, y1: Int, x2: Int, y2: Int, colorFrom: Int, colorTo: Int) {
        fillGradient(poseStack, x1, y1, x2, y2, colorFrom, colorTo, 0)
    }

    fun fillGradient(
        poseStack: PoseStack,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        colorFrom: Int,
        colorTo: Int,
        blitOffset: Int
    ) {
        RenderSystem.disableTexture()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShader { GameRenderer.getPositionColorShader() }
        val tesselator = Tesselator.getInstance()
        val bufferBuilder = tesselator.builder
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
        fillGradient(
            poseStack.last().pose(),
            bufferBuilder,
            x1,
            y1,
            x2,
            y2,
            blitOffset,
            colorFrom,
            colorTo
        )
        tesselator.end()
        RenderSystem.disableBlend()
        RenderSystem.enableTexture()
    }

    fun fillGradient(
        matrix: Matrix4f,
        builder: BufferBuilder,
        x1: Int,
        y1: Int,
        x2: Int,
        y2: Int,
        blitOffset: Int,
        colorA: Int,
        colorB: Int
    ) {
        val f = (colorA shr 24 and 0xFF).toFloat() / 255.0f
        val g = (colorA shr 16 and 0xFF).toFloat() / 255.0f
        val h = (colorA shr 8 and 0xFF).toFloat() / 255.0f
        val i = (colorA and 0xFF).toFloat() / 255.0f
        val j = (colorB shr 24 and 0xFF).toFloat() / 255.0f
        val k = (colorB shr 16 and 0xFF).toFloat() / 255.0f
        val l = (colorB shr 8 and 0xFF).toFloat() / 255.0f
        val m = (colorB and 0xFF).toFloat() / 255.0f
        builder.vertex(matrix, x2.toFloat(), y1.toFloat(), blitOffset.toFloat()).color(g, h, i, f).endVertex()
        builder.vertex(matrix, x1.toFloat(), y1.toFloat(), blitOffset.toFloat()).color(g, h, i, f).endVertex()
        builder.vertex(matrix, x1.toFloat(), y2.toFloat(), blitOffset.toFloat()).color(k, l, m, j).endVertex()
        builder.vertex(matrix, x2.toFloat(), y2.toFloat(), blitOffset.toFloat()).color(k, l, m, j).endVertex()
    }
}
