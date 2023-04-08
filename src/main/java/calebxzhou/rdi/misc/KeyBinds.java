package calebxzhou.rdi.misc;

import calebxzhou.rdi.consts.RdiConsts;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;



public class KeyBinds  {

    public static KeyMapping HOME_KEY;
    public static void init(){
        HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("回岛", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H, RdiConsts.MODID_DISPLAY));
    }

    public static void handleKeyActions(ClientLevel world) {
        Minecraft client = Minecraft.getInstance();
        if(client.isLocalServer() || client.getCurrentServer()==null || client.player==null)
            return;
        //按下H键返回空岛
        while (KeyBinds.HOME_KEY.consumeClick()) {
            String cmd;
            if(client.player.isCrouching())
                cmd="home1";
            else
                cmd="home";
            client.player.commandUnsigned(cmd);
        }
    }
}
