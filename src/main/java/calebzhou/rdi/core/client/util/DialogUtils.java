package calebzhou.rdi.core.client.util;

import calebzhou.rdi.core.client.misc.RdiSystemTray;
import net.minecraft.Util;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.*;


public class DialogUtils {
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

    public static void showMessageBox(String type,String title,String msg){
        TinyFileDialogs.tinyfd_messageBox(title,msg,"ok",type,true);
    }
    public static void showMessageBox(String type,String msg){
        showMessageBox(type,"",msg);
    }
    public static void showPopup(TrayIcon.MessageType type, String msg){
        showPopup(type,"",msg);
    }
    public static void showPopup(TrayIcon.MessageType type, String title, String msg){
        if(Util.getPlatform()== Util.OS.WINDOWS)
            RdiSystemTray.trayIcon.displayMessage(title,msg,type);
        //TinyFileDialogs.tinyfd_notifyPopup(title,msg,type);
    }
   /* public static boolean showYesNo(String msg){
        return TinyFileDialogs.tinyfd_messageBox("提示",msg,"yesno","question",true);
    }*/
}
