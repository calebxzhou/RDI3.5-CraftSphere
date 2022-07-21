package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class Leap implements WorldTickable {
    public Leap() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
    }

    private static final ResourceLocation LEAP_NETWORK=new ResourceLocation(MODID,"leap");
    @Override
    public void tickWorld(ClientLevel world) {

        Minecraft client = Minecraft.getInstance();
        if (KeyBinds.LEAP_KEY.consumeClick()){
            BlockPos lookingAtBlock = PlayerUtils.getPlayerLookingAtBlock(client.player,false);
            if(lookingAtBlock==null){
                return;
            }
            if(client.player.getLevel().getBlockState(lookingAtBlock).getBlock() == Blocks.AIR){
                return;
            }
            NetworkUtils.sendPacketC2S(LEAP_NETWORK,lookingAtBlock.asLong()+"");
        }

    }
}
