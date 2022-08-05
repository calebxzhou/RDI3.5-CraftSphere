package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.screen.NewTitleScreen;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;

public class ServerConnector {
    public static void connect(){


        ConnectScreen.startConnecting(NewTitleScreen.INSTANCE, Minecraft.getInstance(), ExampleMod.SERVER_ADDRESS,ExampleMod.SERVER_INFO);
    }
}
