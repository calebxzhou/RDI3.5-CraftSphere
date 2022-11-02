package calebzhou.rdi.core.client.screen

import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.loader.LoadProgressRecorder
import calebzhou.rdi.core.client.misc.MusicPlayer
import calebzhou.rdi.core.client.util.DialogUtils
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.OptionsScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen
import net.minecraft.network.chat.Component
import net.minecraft.sounds.SoundSource
import java.io.File

class RdiTitleScreen : Screen(Component.literal("主界面")) {
    init {
        Minecraft.getInstance().updateTitle()
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }

    override fun render(matrices: PoseStack, mouseX: Int, mouseY: Int, delta: Float) {
        GlStateManager._clearColor(0.9f, 0.9f, 0.9f, 1.0f)
        GlStateManager._clear(16384, Minecraft.ON_OSX)
        RenderSystem.enableBlend()
        font.draw(matrices, "按Enter(回车)键", width / 2.0f - 30, height / 2f, -0x1000000)
        font.draw(matrices,  "载入用时${"%.2f".format(LoadProgressRecorder.getLoadTimeSeconds())}秒,超越了${"%.2f".format(LoadProgressRecorder.getLoadTimePercentBeyondPlayers())}%的玩家！", 0f, 0f, -0x1000000)
    }

    override fun tick() {
        val handle = Minecraft.getInstance().window.window
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_0)) {
            if (RdiSharedConstants.DEBUG) minecraft!!.setScreen(JoinMultiplayerScreen(this))
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
                DialogUtils.showMessageBox("error", "必须安装ModMenu模组以使用本功能！！")
                e.printStackTrace()
            }
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_O)) {
            MusicPlayer.playOggAsync(File(RdiSharedConstants.RDI_SOUND_FOLDER, "settings.ogg"))
            minecraft!!.setScreen(OptionsScreen(this, minecraft!!.options))
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_P)) {
            minecraft!!.setScreen(PasswordScreen())
            return
        }
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_RETURN) || InputConstants.isKeyDown(
                handle,
                InputConstants.KEY_NUMPADENTER
            )
        ) {
            try {
                if(LoadProgressRecorder.musicPlayJob!=null)
                    LoadProgressRecorder.musicPlayJob!!.stop()
            }catch (_:ThreadDeath){}
            catch (e:Exception) {
                e.printStackTrace()
            }
            IslandLoadScreen()
        }
    }

    public override fun init() {
        Minecraft.getInstance().options.setSoundCategoryVolume(SoundSource.MUSIC, 0f)
    }


}
