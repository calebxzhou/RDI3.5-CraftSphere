package calebzhou.rdi.core.client.screen

import calebzhou.rdi.core.client.RdiCore
import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.misc.HwSpec
import calebzhou.rdi.core.client.misc.MusicPlayer
import calebzhou.rdi.core.client.texture.Textures
import calebzhou.rdi.core.client.util.DialogUtils
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.ImageButton
import net.minecraft.client.gui.screens.GenericDirtMessageScreen
import net.minecraft.client.gui.screens.OptionsScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import java.io.File
import java.util.*

class RdiPauseScreen : Screen(Component.literal("菜单")) {

    override fun init() {
        initWidgets()
    }

    private fun initWidgets() {
        val w = width / 2 - 30
        val j = height / 2 - 50
        addRenderableWidget(
            ImageButton(
                w - 25,
                j,
                20,
                20,
                0,
                0,
                20,
                Textures.ICON_CONTINUE,
                32,
                64,
                { button: Button? ->
                    minecraft!!.setScreen(null)
                    minecraft!!.mouseHandler.grabMouse()
                },
                Component.translatable("menu.returnToGame")
            )
        )
        addRenderableWidget(ImageButton(w, j, 20, 20, 0, 0, 20, Textures.ICON_SETTINGS, 32, 64, { button: Button? ->
            MusicPlayer.playOggAsync(File(RdiSharedConstants.RDI_SOUND_FOLDER, "settings.ogg"))
            minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options))
        }, Component.translatable("menu.options")))
        addRenderableWidget(ImageButton(w + 25, j, 20, 20, 0, 0, 20, Textures.ICON_MODMENU, 32, 64, { button: Button? ->
            try {
                minecraft!!.setScreen(
                    Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(
                        Screen::class.java
                    ).newInstance(this) as Screen
                )
            } catch (e: Exception) {
                DialogUtils.showMessageBox("error", "必须安装ModMenu模组以使用本功能！！")
                e.printStackTrace()
            }
        }, Component.translatable("menu.options")))
        addRenderableWidget(ImageButton(w + 50, j, 20, 20, 0, 0, 20, Textures.ICON_QUIT, 32, 64, { button: Button ->
            val bl = minecraft!!.isLocalServer
            button.active = false
            minecraft!!.level!!.disconnect()
            if (bl) {
                minecraft!!.clearLevel(GenericDirtMessageScreen(Component.translatable("menu.savingLevel")))
            } else {
                minecraft!!.clearLevel()
            }
            MusicPlayer.playOggAsync(File(RdiSharedConstants.RDI_SOUND_FOLDER, "disconnect.ogg"))
            minecraft!!.setScreen(RdiTitleScreen())
        }, Component.translatable("menu.disconnect")))
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {

        //if (this.showMenu) {
        this.renderBackground(matrices)
        drawCenteredString(matrices, font, title, width / 2, 40, 0xffffff)
        val fontColor = 0xeeeeee
        val width = 40
        val height = height - 40
        drawString(matrices, font, "CPU " + HwSpec.currentHwSpec!!.cpu, width, height, fontColor)
        drawString(matrices, font, "显卡 " + HwSpec.currentHwSpec!!.gpu, width, height - 10, fontColor)
        drawString(matrices, font, "内存 " + HwSpec.currentHwSpec!!.mem, width, height - 10 * 2, fontColor)
        val player = Minecraft.getInstance().player ?: return
        val playerInfo = player.connection.getPlayerInfo(UUID.fromString(player.stringUUID)) ?: return
        val latency = playerInfo.latency
        val geoLocation = RdiCore.currentGeoLocation
        var isp = ""
        var province = ""
        if (geoLocation != null) {
            isp = geoLocation.isp
            province = geoLocation.province.substring(0, 2)
        }
        //避免“广东广东广电”这样的情况，运营商里面包括省份的时候，把省份删除
        if (isp.contains(province)) {
            isp = isp.replace(province, "")
        }
        val carrier = province + isp
        val networkDetails = carrier + " " + latency + "ms"
        drawString(matrices, font, "网络 $networkDetails", width, height - 10 * 3, fontColor)
        RdiPingNumberDisplay.renderPingIcon(this, matrices, 16, width - 20, height - 10 * 3, latency)
        /* } else {
            drawCenteredString(matrices, this.font, this.title, this.width / 2, 10, 16777215);
        }*/super.render(matrices, mouseX, mouseY, delta)
    }
}
