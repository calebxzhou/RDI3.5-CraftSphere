package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class ServerConnector {
    public static void connect(){
        Minecraft.getInstance().getWindow().setTitle(ExampleMod.MODID_CHN+" "+ ExampleMod.VER_DISPLAY);
        ConnectScreen.startConnecting(new TitleScreen(), Minecraft.getInstance(), ExampleMod.SERVER_ADDRESS,ExampleMod.SERVER_INFO);
    }
}
