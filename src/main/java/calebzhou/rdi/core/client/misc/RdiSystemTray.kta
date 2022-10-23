package calebzhou.rdi.core.client.misc

import calebzhou.rdi.core.client.RdiSharedConstants
import calebzhou.rdi.core.client.util.AwtImageUtils.createImage
import net.minecraft.Util
import java.awt.AWTException
import java.awt.Image
import java.awt.SystemTray
import java.awt.TrayIcon

object RdiSystemTray {
    @JvmField
	var trayIcon: TrayIcon? = null
    @JvmStatic
	fun createTray() {
        if (Util.getPlatform() !== Util.OS.WINDOWS) return
        if (!SystemTray.isSupported()) {
            println("不支持系统托盘！")
            return
        }
        //final PopupMenu popup = new PopupMenu();
        val iconImage = createImage("/assets/rdict3/icon/icon.gif", RdiSharedConstants.MODID_DISPLAY)
        val trayIconWidth = TrayIcon(iconImage).size.width
        val scaledInstance = iconImage!!.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH)
        trayIcon = TrayIcon(scaledInstance, RdiSharedConstants.MODID_DISPLAY)
        val tray = SystemTray.getSystemTray()
        trayIcon!!.toolTip = RdiSharedConstants.MODID_DISPLAY
        // Create a pop-up menu components
        /*MenuItem aboutItem = new MenuItem("About");
        CheckboxMenuItem cb1 = new CheckboxMenuItem("Set auto size");
        CheckboxMenuItem cb2 = new CheckboxMenuItem("Set tooltip");
        Menu displayMenu = new Menu("Display");
        MenuItem errorItem = new MenuItem("Error");
        MenuItem warningItem = new MenuItem("Warning");
        MenuItem infoItem = new MenuItem("Info");
        MenuItem noneItem = new MenuItem("None");
        MenuItem exitItem = new MenuItem("Exit");

        //Add components to pop-up menu
        popup.add(aboutItem);
        popup.addSeparator();
        popup.add(cb1);
        popup.add(cb2);
        popup.addSeparator();
        popup.add(displayMenu);
        displayMenu.add(errorItem);
        displayMenu.add(warningItem);
        displayMenu.add(infoItem);
        displayMenu.add(noneItem);
        popup.add(exitItem);

        trayIcon.setPopupMenu(popup);*/try {
            tray.add(trayIcon)
        } catch (e: AWTException) {
            println("TrayIcon could not be added.")
        }
    }
}
