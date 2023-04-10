package calebxzhou.libertorch.mc.gui

import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.util.Gl
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import com.mojang.math.Matrix4f
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiComponent
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.network.chat.Component

/**
 * Created  on 2023-03-01,16:23.
 */
open class LtScreen : Screen {

    constructor(title: Component) : super(title)
    constructor(title: String): super(Component.literal(title))


    companion object{
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
            Gl.clearColor(DefaultColors.PineGreen.color)
        }
        fun fillGradient(poseStack: PoseStack, x1: Int, y1: Int, x2: Int, y2: Int, colorFrom: Int, colorTo: Int) {
            fillGradient(poseStack, x1, y1, x2, y2, colorFrom, colorTo, 0)
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
            val matrix = poseStack.last().pose()

            fillGradient(
                matrix,
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

        @JvmStatic
        fun renderTooltip(
            poseStack: PoseStack,
            clientTooltipComponents: MutableList<ClientTooltipComponent>,
            mouseX: Int,
            mouseY: Int,
            width: Int,
            height: Int,
            itemRenderer: ItemRenderer,

            ) {
            val font = Minecraft.getInstance().font
            if (clientTooltipComponents.isEmpty()) return
            var i = 0
            var j = if (clientTooltipComponents.size == 1) -2 else 0
            for (clientTooltipComponent in clientTooltipComponents) {
                val k = clientTooltipComponent.getWidth(font)
                if (k > i) {
                    i = k
                }
                j += clientTooltipComponent.height
            }
            var l = mouseX + 12
            var m = mouseY - 12
            if (l + i > width) {
                l -= 28 + i
            }
            if (m + j + 6 > height) {
                m = height - j - 6
            }
            poseStack.pushPose()
            val biltOffset: Float = itemRenderer.blitOffset
            itemRenderer.blitOffset = 400.0f
            val tesselator = Tesselator.getInstance()
            val bufferBuilder = tesselator.builder
            RenderSystem.setShader { GameRenderer.getPositionColorShader() }
            bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
            val matrix4f = poseStack.last().pose()


            GuiComponent.fillGradient(
                matrix4f,
                bufferBuilder,
                l - 3,
                m - 3,
                l + i + 3,
                m + j + 3,
                400,
                LtTheme.now.tooltipBgColor.opaque,
                LtTheme.now.tooltipBgColor.opaque,
            )
            GuiComponent.fillGradient(
                matrix4f,
                bufferBuilder,
                l - 3,
                m - 3 + 1,
                l - 3 + 1,
                m + j + 3 - 1,
                400,
                LtTheme.now.tooltipOutlineColor.opaque,
                LtTheme.now.tooltipOutlineColor.opaque
            )
            GuiComponent.fillGradient(
                matrix4f,
                bufferBuilder,
                l + i + 2,
                m - 3 + 1,
                l + i + 3,
                m + j + 3 - 1,
                400,
                LtTheme.now.tooltipOutlineColor.opaque,
                LtTheme.now.tooltipOutlineColor.opaque,
            )
            GuiComponent.fillGradient(
                matrix4f,
                bufferBuilder,
                l - 3,
                m - 3,
                l + i + 3,
                m - 3 + 1,
                400,
                LtTheme.now.tooltipOutlineColor.opaque,
                LtTheme.now.tooltipOutlineColor.opaque
            )
            GuiComponent.fillGradient(
                matrix4f,
                bufferBuilder,
                l - 3,
                m + j + 2,
                l + i + 3,
                m + j + 3,
                400,
                LtTheme.now.tooltipOutlineColor.opaque,
                LtTheme.now.tooltipOutlineColor.opaque
            )
            RenderSystem.enableDepthTest()
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            BufferUploader.drawWithShader(bufferBuilder.end())
            RenderSystem.disableBlend()
            RenderSystem.enableTexture()
            val bufferSource = MultiBufferSource.immediate(Tesselator.getInstance().builder)
            poseStack.translate(0.0, 0.0, 400.0)
            var s = m
            for (t in clientTooltipComponents.indices) {
                val clientTooltipComponent2 = clientTooltipComponents[t]
                clientTooltipComponent2.renderText(font, l, s, matrix4f, bufferSource)
                s += clientTooltipComponent2.height + if (t == 0) 2 else 0
            }
            bufferSource.endBatch()
            poseStack.popPose()
            s = m
            for (t in clientTooltipComponents.indices) {
                val clientTooltipComponent2 = clientTooltipComponents[t]
                clientTooltipComponent2.renderImage(font, l, s, poseStack, itemRenderer, 400)
                s += clientTooltipComponent2.height + if (t == 0) 2 else 0
            }
            itemRenderer.blitOffset = biltOffset
        }
    }
}
