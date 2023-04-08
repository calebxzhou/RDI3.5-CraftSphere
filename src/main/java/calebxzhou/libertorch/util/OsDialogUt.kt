package calebxzhou.libertorch.util

import org.lwjgl.util.tinyfd.TinyFileDialogs

/**
 * Created  on 2023-04-08,19:40.
 */
object OsDialogUt {
    @JvmStatic
    fun showYesNoBox(msg: String): Boolean {
        return TinyFileDialogs.tinyfd_messageBox("提示", msg, "yesno", "question", false)
    }
    @JvmStatic
    fun showMessageBox(type: String, title: String, msg: String) {
        TinyFileDialogs.tinyfd_messageBox(title, msg, "ok", type, true)
    }
    @JvmStatic
    fun showMessageBox(type: String, msg: String) {
        showMessageBox(type, "", msg)
    }
    @JvmStatic
    fun showPopup(type: String, msg: String) {
        showPopup(type, "", msg)
    }
    //info warning error
    @JvmStatic
    fun showPopup(type: String, title: String, msg: String) {
        TinyFileDialogs.tinyfd_notifyPopup(title,msg,type);
    }
}
