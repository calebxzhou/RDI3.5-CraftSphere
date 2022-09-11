package calebzhou.rdi.core.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class ChatUtils {

    public static void addMessage(String msg){
        addMessage(Component.literal(msg));
    }
    public static void addMessage(Component component){
        Minecraft.getInstance().gui.getChat().addMessage(component);
    }
}
