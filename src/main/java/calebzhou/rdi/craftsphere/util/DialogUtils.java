package calebzhou.rdi.craftsphere.util;

import calebzhou.rdi.craftsphere.dialog.InfoToast;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class DialogUtils {
    public static void showInfoIngame(String msg){
        showInfoIngame(new LiteralText(msg));
    }
    public static void showInfoIngame(Text msg){
        InfoToast toast = InfoToast.create(msg,null);
        ToastManager toastManager = MinecraftClient.getInstance().getToastManager();
        toastManager.clear();
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
