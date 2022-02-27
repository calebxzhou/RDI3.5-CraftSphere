package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.dialog.InfoToast;
import calebzhou.rdi.craftsphere.model.MessageType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

import java.awt.*;

public class DialogUtils {
    public static void showInfoIngame(String msg, MessageType type){
        Color color=Color.WHITE;
        switch (type){
            case INFO -> color=Color.WHITE;
            case ERROR -> color= Color.RED.brighter();
            case SUCCESS -> color= Color.GREEN.brighter();
        }
        showInfoIngame(new LiteralText(msg),color);
    }
    public static void showInfoIngame(Text msg,Color color){
        InfoToast toast = InfoToast.create(msg,null,color.getRGB());
        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
        toastManager.add(toast);
    }
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
