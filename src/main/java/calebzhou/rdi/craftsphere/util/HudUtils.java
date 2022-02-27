package calebzhou.rdi.craftsphere.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

public class HudUtils {
    //聊天框中插入一条信息
    public static void addChatBoxMessage(String msg){
        addChatBoxMessage(new LiteralText(msg));
    }
    public static void addChatBoxMessage(Text msg){
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(msg);
    }
}
