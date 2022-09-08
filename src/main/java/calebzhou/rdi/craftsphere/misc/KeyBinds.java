package calebzhou.rdi.craftsphere.misc;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;

import static calebzhou.rdi.craftsphere.RdiCore.MODID_CHN;


public class KeyBinds  {

    public static KeyMapping HOME_KEY;
    public static void init(){
        HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("回岛", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H, MODID_CHN));
        /*SLOWFALL_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("缓降", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J, MODID_CHN));
        LEAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("隔空跳", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G, MODID_CHN));*/
    }

    public static void handleKeyActions(ClientLevel world) {
        Minecraft client = Minecraft.getInstance();
        if(client.isLocalServer() || client.getCurrentServer()==null || client.player==null)
            return;
        //按下H键返回空岛
        while (KeyBinds.HOME_KEY.consumeClick()) {
            String cmd;
            if(client.player.isCrouching())
                cmd="home";
            else
                cmd="home2";
            client.player.commandUnsigned(cmd);
        }
        //按下J键开启缓降
        /*while (KeyBinds.SLOWFALL_KEY.consumeClick()) {
            client.player.command("slowfall 1");
        }*/

    }
}
