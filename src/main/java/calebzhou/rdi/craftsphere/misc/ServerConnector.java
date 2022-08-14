package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.screen.RdiTitleScreen;
import calebzhou.rdi.craftsphere.util.ThreadPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import java.net.UnknownHostException;

public class ServerConnector {
    public static final ServerStatusPinger PINGER = new ServerStatusPinger();
    public static final ServerAddress SERVER_ADDRESS = ExampleMod.debug?new ServerAddress("localhost",25565):new ServerAddress("test3.davisoft.cn",26085);
    public static final ServerData SERVER_INFO = new ServerData(ExampleMod.MODID_CHN+ExampleMod.VER_DISPLAY, SERVER_ADDRESS.getHost()+":"+SERVER_ADDRESS.getPort(),false);
    public static void connect(){
        Minecraft.getInstance().getWindow().setTitle(ExampleMod.MODID_CHN+" "+ ExampleMod.VER_DISPLAY);
        ConnectScreen.startConnecting(RdiTitleScreen.INSTANCE, Minecraft.getInstance(), SERVER_ADDRESS,SERVER_INFO);
    }
    public static void ping(){
        ThreadPool.newThread(()->{
            try {
                PINGER.pingServer(SERVER_INFO, () -> {});
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }
}
