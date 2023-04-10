package calebxzhou.rdi.screen

import calebxzhou.libertorch.mc.gui.LtTheme
import calebxzhou.libertorch.mc.gui.components.LtTextButton
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
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource

class RdiTitleScreen : Screen(Component.literal("主界面")) {
    private lateinit var  startBtn : Button
    private lateinit var  settingsBtn: Button
    private lateinit var  aboutBtn: Button
    private lateinit var  rdidBtn: Button


    public override fun init() {
        Minecraft.getInstance().updateTitle()
        //关闭音乐
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC, 0f)

        aboutBtn = PlainTextButton(width-25, height-10,50,20, Component.literal("v${CoreVersion}"),::onClickAbout,font)
        val usrName = RdiUser.now.name
        rdidBtn = LtTextButton(width / 2 +20, height / 3, Component.literal(usrName),::clickRdid,::tipRdid)
        addRenderableWidget(aboutBtn)
        addRenderableWidget(rdidBtn)
    }

    private fun tipRdid(button: Button, poseStack: PoseStack, x: Int, y: Int) {
        renderComponentTooltip(poseStack,
            listOf(
                Component.literal("RDID: ${RdiUser.now.rdid}"),
                Component.literal("点此管理您的RDI账号")
            ),x,y)
    }

    private fun clickRdid(button: Button) {

    }

    private fun onClickAbout(button: Button) {
        Minecraft.getInstance().setScreen((this))
    }
    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        Gl.clearColor(LtTheme.now.bgColor)
        val skin = Minecraft.getInstance().skinManager.getInsecureSkinLocation(RdiUser.now.gameProfile)
        RenderSystem.setShaderTexture(0, skin)
        PlayerFaceRenderer.draw(poseStack, width/2, height/3-5, 16,false,false);
        font.draw(poseStack, "${TimeUt.periodOfDay()}好，", width / 2.0f - 36, height / 3f, 0xFFFFFF)
        font.draw(poseStack, "按下Enter键。", width / 2.0f - 36, height / 2f+20, 0xFFFFFF)

        super.render(poseStack, mouseX, mouseY, partialTick)
        //font.draw(matrices, "按Enter(回车)键", width / 2.0f - 30, height / 2f, DefaultColors.White.color.hex)
        /*font.draw(matrices,  "载入用时${"%.2f".format(LoadProgressRecorder.getLoadTimeSeconds())}秒,超越了${"%.2f".format(
            LoadProgressRecorder.getLoadTimePercentBeyondPlayers())}%的玩家！", 0f, 0f, -0x1000000)*/
    }

    override fun tick() {
        val handle = Minecraft.getInstance().window.window
        when {
            InputConstants.isKeyDown(handle, InputConstants.KEY_0) -> {
                if (DEBUG) minecraft!!.setScreen(JoinMultiplayerScreen(this))
                return
            }
            InputConstants.isKeyDown(handle, InputConstants.KEY_K) -> {
                minecraft!!.setScreen(SelectWorldScreen(this))
                return
            }
            InputConstants.isKeyDown(handle, InputConstants.KEY_M) -> {
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
            InputConstants.isKeyDown(handle, InputConstants.KEY_O) -> {
                SoundPlayer.playOgg(RdiSounds.Settings)
                minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options))
                return
            }
            InputConstants.isKeyDown(handle, InputConstants.KEY_P) -> {
                minecraft!!.setScreen(PasswordScreen())
                return
            }
            InputConstants.isKeyDown(handle, InputConstants.KEY_0) -> {
                minecraft!!.setScreen(JoinMultiplayerScreen(this))
                return
            }
            InputConstants.isKeyDown(handle, InputConstants.KEY_RETURN) || InputConstants.isKeyDown(
                handle,
                InputConstants.KEY_NUMPADENTER
            ) -> {
                ServerConnector.connect()
            }
        }
    }


}
