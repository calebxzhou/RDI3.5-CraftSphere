package calebxzhou.rdi.screen

import calebxzhou.rdi.RdiCore
import calebxzhou.rdi.misc.ServerConnector
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.Util
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Overlay
import net.minecraft.server.packs.resources.ReloadInstance
import net.minecraft.util.FastColor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.input.ReversedLinesFileReader
import org.apache.commons.lang3.RandomUtils
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.function.Consumer
import kotlin.math.roundToInt

class RdiLoadingOverlay(minecraft: Minecraft, reload: ReloadInstance, exceptionHandler: Consumer<Optional<Throwable>>) :
    Overlay() {
    private val startTime: Long
    private var fadeOutStart = -1L
    private val minecraft: Minecraft
    private val reload: ReloadInstance
    private val onFinish: Consumer<Optional<Throwable>>
    override fun isPauseScreen(): Boolean {
        return true
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {

        val scaledWidth = minecraft.window.guiScaledWidth
        val scaledHeight = minecraft.window.guiScaledHeight
        val timeNow = System.currentTimeMillis()
        val f = if (fadeOutStart > -1L) (timeNow - fadeOutStart).toFloat() / 1000.0f else -1.0f
        GlStateManager._clearColor(0.9f, 0.9f, 0.9f, 1.0f)
        GlStateManager._clear(16384, Minecraft.ON_OSX)
        RenderSystem.enableBlend()
        if (f < 1.0f) {
            drawProgressBar(poseStack, scaledHeight / 2 - 5, scaledWidth, scaledHeight / 2 + 5, timeNow)
        }
        if (f >= 1.0f) {
            minecraft.overlay = null
        }
        if (fadeOutStart == -1L && reload.isDone) {
            try {
                reload.checkExceptions()
                onFinish.accept(Optional.empty())
            } catch (var23: Throwable) {
                onFinish.accept(Optional.of(var23))
            }
            fadeOutStart = Util.getMillis()
            if (minecraft.screen != null) {
                minecraft.screen!!.init(minecraft, minecraft.window.guiScaledWidth, minecraft.window.guiScaledHeight)
            }
        }
    }

    //到3秒 加载条就弹回去
    private var maximumLoadBarLength = 3000f

    init {
        //ping一下服务器提高载入速度
        ServerConnector.ping()
        this.minecraft = minecraft
        this.reload = reload
        onFinish = exceptionHandler
        startTime = System.currentTimeMillis()
    }

    private fun drawProgressBar(matrices: PoseStack, minY: Int, maxX: Int, maxY: Int, timeNow: Long) {
        var timeRatio = (timeNow - startTime) / maximumLoadBarLength
        if (timeRatio > 1) {
            timeRatio = timeRatio - timeRatio.toInt()
            maximumLoadBarLength = RandomUtils.nextFloat(2700f, 4500f)
        }
        try {
            val logLastLine = try {
                ReversedLinesFileReader(File("./logs/debug.log"), StandardCharsets.UTF_8).readLine()
            } catch (e: Exception) {
                ""
            }
            if(RdiCore.gameReady)
                Minecraft.getInstance().font.draw(matrices,logLastLine,0f,0f,0x000000)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        val progressToDisplayPixels = (maxX * timeRatio).roundToInt()
        val barColor = FastColor.ARGB32.color(255, 64, 64, 64)
        fill(matrices, 2, minY + 2, progressToDisplayPixels, maxY - 2, barColor)
    }
}
