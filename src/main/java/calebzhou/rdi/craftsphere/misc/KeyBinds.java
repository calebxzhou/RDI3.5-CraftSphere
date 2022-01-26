package calebzhou.rdi.craftsphere.misc;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.lwjgl.glfw.GLFW;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID_CHN;


public class KeyBinds {

    public static KeyBinding HOME_KEY;
    public static KeyBinding SLOWFALL_KEY;
    public static KeyBinding LEAP_KEY;
    public static void init(){
        HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("回到空岛", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H, MODID_CHN));
        SLOWFALL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("缓降", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_J, MODID_CHN));
        LEAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("隔空跳跃", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_G, MODID_CHN));
    }
    public static void notifyPlayer(KeyBinding key,ClientPlayerEntity player){
        player.sendMessage(new TranslatableText("您按下了“"+key.getTranslationKey()+"”快捷键。"),false);
    }
    /*public static final KeyBinding SLOWFALL = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "回到空岛", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H, MODID));*/


}
