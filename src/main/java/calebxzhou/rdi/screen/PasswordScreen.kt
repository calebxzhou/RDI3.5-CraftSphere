package calebxzhou.rdi.screen

import calebxzhou.libertorch.util.OsDialogUt
import calebxzhou.rdi.RdiCore
import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.EditBox
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component

/**
 * Created  on 2022-10-22,20:10.
 */
class PasswordScreen: Screen(Component.literal("输入密码屏幕")) {
    private val editbox:EditBox= EditBox(font,300,100,100,20, null, Component.literal("输入密码"))


    override fun tick() {
        val handle = Minecraft.getInstance().window.window
        editbox.tick()
        if (InputConstants.isKeyDown(handle, InputConstants.KEY_RETURN) || InputConstants.isKeyDown(
                handle,
                InputConstants.KEY_NUMPADENTER
            )
        ){
            OsDialogUt.showPopup("info", "您成功设定密码为${editbox.value},即将回到主界面！")
            RdiCore.currentRdiUser!!.pwd=editbox.value
            RdiCore.currentRdiUser!!.writePasswordToFile()
            minecraft!!.setScreen(RdiTitleScreen())
        }
    }

    override fun init() {
        addWidget(editbox)
        setInitialFocus(editbox)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        return  if(super.keyPressed(keyCode, scanCode, modifiers))true else editbox.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun onClose() {
        minecraft!!.setScreen(RdiTitleScreen())
    }
    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        GlStateManager._clearColor(0.9f, 0.9f, 1f, 1.0f)
        GlStateManager._clear(16384, Minecraft.ON_OSX)
        RenderSystem.enableBlend()
        editbox.render(poseStack, mouseX, mouseY, partialTick)
        drawCenteredString(poseStack,font,"输入密码。输完按Enter(回车)。别用鼠标点输入框，直接按键盘！",this.width/2,8,0xffffff)
        super.render(poseStack, mouseX, mouseY, partialTick)
    }
}
