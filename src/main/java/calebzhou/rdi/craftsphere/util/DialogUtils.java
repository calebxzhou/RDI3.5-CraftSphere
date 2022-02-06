package calebzhou.rdi.craftsphere.util;

import org.lwjgl.util.tinyfd.TinyFileDialogs;

public class DialogUtils {
    public static void showError(String msg){
        TinyFileDialogs.tinyfd_notifyPopup("RDI-错误",msg,"info");
    }
}
