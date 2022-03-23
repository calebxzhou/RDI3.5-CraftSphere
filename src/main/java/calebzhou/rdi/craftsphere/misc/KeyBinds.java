package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.module.area.ModelAreaSelection;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import org.lwjgl.glfw.GLFW;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID_CHN;


public class KeyBinds  {

    public static KeyMapping HOME_KEY;
    public static KeyMapping SLOWFALL_KEY;
    public static KeyMapping LEAP_KEY;
    //物品栏跳9格
    public static KeyMapping HOTBAR_JUMP_9;
    //物品栏跳同类
    public static KeyMapping HOTBAR_JUMP_SAME;
   // public static KeyBinding AREA_SELECTION_KEY;
    public static void init(){
        HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("回岛", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_H, MODID_CHN));
        SLOWFALL_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("缓降", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_J, MODID_CHN));
        LEAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("隔空跳", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_G, MODID_CHN));
        HOTBAR_JUMP_9 = KeyBindingHelper.registerKeyBinding(new KeyMapping("物品栏跳9格", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_C, MODID_CHN));
        HOTBAR_JUMP_SAME = KeyBindingHelper.registerKeyBinding(new KeyMapping("物品栏跳同类", InputConstants.Type.KEYSYM,GLFW.GLFW_KEY_V, MODID_CHN));
     //   AREA_SELECTION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("进入区域选择模式(金锄头)", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_K, MODID_CHN));
    }

    public static void handleKeyActions(ClientLevel world) {
        Minecraft client = Minecraft.getInstance();
        if(client.isLocalServer() || client.getCurrentServer()==null || client.player==null)
            return;
        //按下H键返回空岛
        while (KeyBinds.HOME_KEY.consumeClick()) {
            client.player.chat("/home");
        }
        //按下J键开启缓降
        while (KeyBinds.SLOWFALL_KEY.consumeClick()) {
            client.player.chat("/slowfall 1");
        }
        /*while (KeyBinds.AREA_SELECTION_KEY.wasPressed()){
            if(!ModelAreaSelection.isAreaSelectionMode){
                ModelAreaSelection.isAreaSelectionMode =true;
            }else{
                ModelAreaSelection.isAreaSelectionMode =false;
            }
        }*/

    }
}
