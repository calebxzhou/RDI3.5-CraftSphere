package calebxzhou.rdi.util

import calebxzhou.libertorch.util.OsDialogUt.showYesNoBox
import calebxzhou.rdi.NetTx
import calebxzhou.rdi.RdiCore
import calebxzhou.rdi.consts.NetPacks
import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.Window
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW

/**
 * Created  on 2023-04-08,19:42.
 */
object WindowUt {
    /*public static void showInfoIngame(String msg, MessageType type){
        Color color= Color.WHITE;
        switch (type){
            case INFO -> color=Color.WHITE;
            case ERROR -> color= Color.RED.brighter();
            case SUCCESS -> color= Color.GREEN.brighter();
        }
        showInfoIngame(Component.literal(msg),color);
    }*/
    /*public static void showInfoIngame(Component msg,Color color){
        Toast toast = SystemToast.create(msg,null,color.getRGB());
        ToastComponent toastManager = Minecraft.getInstance().getToasts();
        toastManager.addToast(toast);
    }*/

    @JvmStatic
    fun shouldClose(window: Window):Boolean{
        if (GLX._shouldClose(window)) {
            val rain: String = RdiCore.currentWeather?.realTimeWeather?.rainDesc?:""
            return if (showYesNoBox("真的要退出RDI客户端吗？\n$rain")) {
                if(Minecraft.getInstance().level!=null)
                    NetTx.send(NetPacks.SAVE_WORLD,1);
                GLFW.glfwSetWindowShouldClose(Minecraft.getInstance().window.window, true)
                true
            } else {
                GLFW.glfwSetWindowShouldClose(Minecraft.getInstance().window.window, false)
                false
            }
        }
        return false
    }
}
