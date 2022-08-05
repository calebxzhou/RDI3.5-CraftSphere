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
    public static void main(String[] args) {

    }
    public static void showMessageBox(String type,String title,String msg){
        TinyFileDialogs.tinyfd_messageBox(title,msg,"ok",type,true);
    }
    public static void showMessageBox(String type,String msg){
        showMessageBox(type,"",msg);
    }
    public static void showPopup(String type,String msg){
        showPopup(type,"",msg);
    }
    public static void showPopup(String type,String title,String msg){
        TinyFileDialogs.tinyfd_notifyPopup(title,msg,type);
    }
   /* public static boolean showYesNo(String msg){
        return TinyFileDialogs.tinyfd_messageBox("提示",msg,"yesno","question",true);
    }*/
}
