package calebzhou.rdi.craftsphere.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class HudUtils {
    //聊天框中插入一条信息
    public static void addChatBoxMessage(String msg){
        addChatBoxMessage(Component.literal(msg));
    }
    public static void addChatBoxMessage(Component msg){
        Minecraft.getInstance().gui.getChat().addMessage(msg);
    }
}
