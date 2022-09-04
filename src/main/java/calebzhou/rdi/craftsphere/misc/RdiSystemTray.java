package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.RdiCore;
import calebzhou.rdi.craftsphere.util.AwtImageUtils;
import net.minecraft.Util;

import java.awt.*;

public class RdiSystemTray {

    public static TrayIcon trayIcon;
    public static void createTray( ){
        if(Util.getPlatform()!= Util.OS.WINDOWS)
            return;
        if (!SystemTray.isSupported()) {
            System.out.println("不支持系统托盘！");
            return;
        }
        //final PopupMenu popup = new PopupMenu();
        Image iconImage = AwtImageUtils.createImage("/assets/rdict3/icon/icon.gif", RdiCore.MODID_CHN);
        int trayIconWidth = new TrayIcon(iconImage).getSize().width;
        Image scaledInstance = iconImage.getScaledInstance(trayIconWidth, -1, Image.SCALE_SMOOTH);
        trayIcon = new TrayIcon(scaledInstance, RdiCore.MODID_CHN);
        final SystemTray tray = SystemTray.getSystemTray();
        trayIcon.setToolTip(RdiCore.MODID_CHN);
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

        trayIcon.setPopupMenu(popup);*/

        try {
            tray.add(trayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }
}
