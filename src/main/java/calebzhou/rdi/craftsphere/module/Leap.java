package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.misc.KeyBinds;
import calebzhou.rdi.craftsphere.util.NetworkReceivableS2C;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class Leap implements WorldTickable {
    private static final Identifier LEAP_NETWORK=new Identifier(MODID,"leap");
    @Override
    public void tickWorld(ClientWorld world) {
        MinecraftClient client = MinecraftClient.getInstance();
        if(!KeyBinds.LEAP_KEY.wasPressed())
            return;
        BlockPos lookingAtBlock = PlayerUtils.getPlayerLookingAtBlock(client.player,false);
        if(lookingAtBlock==null){
            return;
        }
        if(client.player.getWorld().getBlockState(lookingAtBlock).getBlock() == Blocks.AIR){
            return;
        }
        NetworkUtils.sendPacketC2S(LEAP_NETWORK,lookingAtBlock.asLong()+"");
    }
}
