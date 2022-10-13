@file:JvmName("DialogUtilsKt")
package calebzhou.rdi.core.client.util


import calebzhou.rdi.core.client.misc.RdiSystemTray
import com.mojang.blaze3d.platform.GLX
import com.mojang.blaze3d.platform.Window
import net.minecraft.Util
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.lwjgl.util.tinyfd.TinyFileDialogs
import java.awt.TrayIcon

object DialogUtils {
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
	fun showYesNoBox(type: String?, msg: String?): Boolean {
        return TinyFileDialogs.tinyfd_messageBox("提示", msg, "yesno", "question", false)
    }
    @JvmStatic
    fun showMessageBox(type: String?, title: String?, msg: String?) {
        TinyFileDialogs.tinyfd_messageBox(title, msg, "ok", type, true)
    }
    @JvmStatic
    fun showMessageBox(type: String?, msg: String?) {
        showMessageBox(type, "", msg)
    }
    @JvmStatic
    fun showPopup(type: TrayIcon.MessageType?, msg: String?) {
        showPopup(type, "", msg)
    }
    @JvmStatic
    fun shouldClose(window: Window):Boolean{
        if (GLX._shouldClose(window)) {
            val rain: String
            val currentWeather = RdiWeather.currentWeather
            rain = if (currentWeather == null) "" else currentWeather.minuteRainDescription
            return if (showYesNoBox("yesno", "真的要退出RDI客户端吗？\n$rain")) {
                GLFW.glfwSetWindowShouldClose(Minecraft.getInstance().window.window, true)
                true
            } else {
                GLFW.glfwSetWindowShouldClose(Minecraft.getInstance().window.window, false)
                false
            }
        }
        return false
    }
    @JvmStatic
	fun showPopup(type: TrayIcon.MessageType?, title: String?, msg: String?) {
        if (Util.getPlatform() === Util.OS.WINDOWS) RdiSystemTray.trayIcon!!.displayMessage(title, msg, type)
        //TinyFileDialogs.tinyfd_notifyPopup(title,msg,type);
    } /* public static boolean showYesNo(String msg){
        return TinyFileDialogs.tinyfd_messageBox("提示",msg,"yesno","question",true);
    }*/
}
