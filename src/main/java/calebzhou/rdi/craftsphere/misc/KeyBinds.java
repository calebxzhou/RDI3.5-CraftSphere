package calebzhou.rdi.craftsphere.misc;

import calebzhou.rdi.craftsphere.module.area.ModelAreaSelection;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID_CHN;


public class KeyBinds  {

    public static KeyBinding HOME_KEY;
    public static KeyBinding SLOWFALL_KEY;
    public static KeyBinding LEAP_KEY;
   // public static KeyBinding AREA_SELECTION_KEY;
    public static void init(){
        HOME_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("回到空岛", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_H, MODID_CHN));
        SLOWFALL_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("缓降", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_J, MODID_CHN));
        LEAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("隔空跳跃", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_G, MODID_CHN));
     //   AREA_SELECTION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding("进入区域选择模式(金锄头)", InputUtil.Type.KEYSYM,GLFW.GLFW_KEY_K, MODID_CHN));
    }

    public static void handleKeyActions(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(client.isInSingleplayer() || client.getCurrentServerEntry()==null || client.player==null)
            return;
        //按下H键返回空岛
        while (KeyBinds.HOME_KEY.wasPressed()) {
            client.player.sendChatMessage("/home");
        }
        //按下J键开启缓降
        while (KeyBinds.SLOWFALL_KEY.wasPressed()) {

            client.player.sendChatMessage("/slowfall 1");

        }
        //按下G键隔空跳跃
        while (KeyBinds.LEAP_KEY.wasPressed()) {
            BlockPos lookingAtBlock = PlayerUtils.getPlayerLookingAtBlock(client.player,false);
            if(lookingAtBlock==null){
                return;
            }
            if(client.player.getWorld().getBlockState(lookingAtBlock).getBlock() == Blocks.AIR){
                return;
            }
            client.player.sendChatMessage("/leap "+lookingAtBlock.asLong());
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
