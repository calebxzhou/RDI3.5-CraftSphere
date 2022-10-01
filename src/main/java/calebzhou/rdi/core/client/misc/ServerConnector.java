package calebzhou.rdi.core.client.misc;

import calebzhou.rdi.core.client.RdiSharedConstants;
import calebzhou.rdi.core.client.screen.RdiTitleScreen;
import calebzhou.rdi.core.client.util.ThreadPool;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerStatusPinger;
import net.minecraft.client.multiplayer.resolver.ServerAddress;

import static calebzhou.rdi.core.client.RdiSharedConstants.CORE_VERSION;
import static calebzhou.rdi.core.client.RdiSharedConstants.MODID_DISPLAY;

public class ServerConnector {
    public static final ServerStatusPinger PINGER = new ServerStatusPinger();
    public static final ServerAddress SERVER_ADDRESS = RdiSharedConstants.DEBUG?new ServerAddress("localhost",25565):new ServerAddress("test3.davisoft.cn",26085);
    public static final ServerData SERVER_INFO = new ServerData(MODID_DISPLAY + CORE_VERSION, SERVER_ADDRESS.getHost()+":"+SERVER_ADDRESS.getPort(),false);
    public static void connect(){
        Minecraft.getInstance().getWindow().setTitle(MODID_DISPLAY +" "+ CORE_VERSION);
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
