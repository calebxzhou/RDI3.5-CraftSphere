package calebxzhou.rdi.screen

import calebxzhou.libertorch.mc.gui.LtTheme
import calebxzhou.libertorch.ui.DefaultColors
import calebxzhou.libertorch.sound.SoundPlayer
import calebxzhou.libertorch.util.Gl
import calebxzhou.libertorch.util.OsDialogUt
import calebxzhou.libertorch.util.TimeUt
import calebxzhou.rdi.consts.RdiConsts.CoreVersion
import calebxzhou.rdi.consts.RdiConsts.DEBUG
import calebxzhou.rdi.consts.RdiConsts.ReleaseDate
import calebxzhou.rdi.consts.RdiSounds
import calebxzhou.rdi.misc.ServerConnector
import calebxzhou.rdi.model.RdiUser
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.components.PlainTextButton
import net.minecraft.client.gui.components.PlayerFaceRenderer
import net.minecraft.client.gui.screens.OptionsScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import java.util.*

class RdiTitleScreen : Screen(Component.literal("主界面")) {
    private lateinit var  startBtn : Button
    private lateinit var  settingsBtn: Button
    private lateinit var  aboutBtn: Button


    public override fun init() {
        Minecraft.getInstance().updateTitle()
        //关闭音乐
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC, 0f)
        aboutBtn = PlainTextButton(width-70, height-10,50,20, Component.literal("v${CoreVersion} $ReleaseDate"),::onClickAbout,font)
        addRenderableWidget(aboutBtn)
    }
    private fun onClickAbout(button: Button) {
        Minecraft.getInstance().setScreen((this))
    }
    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        Gl.clearColor(LtTheme.now.bgColor)
        RenderSystem.setShaderTexture(0,  DefaultPlayerSkin.getDefaultSkin(UUID.fromString(RdiUser.now.uuid)))
        PlayerFaceRenderer.draw(matrices, width/2-40, height/2-5, 16,false,false);
        font.draw(matrices, "${TimeUt.periodOfDay()}好，${RdiUser.now.name}。", width / 2.0f - 18, height / 2f, 0xFFFFFF)
        font.draw(matrices, "按下Enter键。", width / 2.0f - 36, height / 2f+20, 0xFFFFFF)
        //font.draw(matrices, "按Enter(回车)键", width / 2.0f - 30, height / 2f, DefaultColors.White.color.hex)
        /*font.draw(matrices,  "载入用时${"%.2f".format(LoadProgressRecorder.getLoadTimeSeconds())}秒,超越了${"%.2f".format(
            LoadProgressRecorder.getLoadTimePercentBeyondPlayers())}%的玩家！", 0f, 0f, -0x1000000)*/
    }

    override fun tick() {
        val handle = Minecraft.getInstance().window.window
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_0)) {
            if (DEBUG) minecraft!!.setScreen(JoinMultiplayerScreen(this))
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_K)) {
            minecraft!!.setScreen(SelectWorldScreen(this))
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_M)) {
            try {
                minecraft!!.setScreen(
                    Class.forName("com.terraformersmc.modmenu.gui.ModsScreen").getConstructor(
                        Screen::class.java
                    ).newInstance(this) as Screen
                )
            } catch (e: Exception) {
                OsDialogUt.showMessageBox("error", "必须安装ModMenu模组以使用本功能！！")
                e.printStackTrace()
            }
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_O)) {
            SoundPlayer.playOgg(RdiSounds.Settings)
            minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options))
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_P)) {
            minecraft!!.setScreen(PasswordScreen())
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_0)) {
            minecraft!!.setScreen(JoinMultiplayerScreen(this))
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_RETURN) || InputConstants.isKeyDown(
                handle,
                InputConstants.KEY_NUMPADENTER
            )
        ) {
            ServerConnector.connect()
        }
    }


}