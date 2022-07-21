package calebzhou.rdi.craftsphere.util;

import org.lwjgl.util.tinyfd.TinyFileDialogs;


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
    public static void showError(String msg){
        TinyFileDialogs.tinyfd_messageBox("错误",msg,"ok","error",true);
    }
    public static void showInfo(String msg){
        TinyFileDialogs.tinyfd_messageBox("提示",msg,"ok","info",true);
    }
    public static boolean showYesNo(String msg){
        return TinyFileDialogs.tinyfd_messageBox("提示",msg,"yesno","question",true);
    }
}
