package calebzhou.rdi.craftsphere.module.fasttree;

import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.PlayerUtils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.SaplingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BoneMealItem;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class FastTree {
    public static final Identifier FAST_TREE_NETWORK =new Identifier(MODID,"fast_tree");
    private int treeScore =0;
    //生长树苗需要200
    private final int requireScore =200;
    public FastTree() {
        ClientTickEvents.END_WORLD_TICK.register(this::handleGrowTree);
    }
    private void handleGrowTree(ClientWorld world){
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        BlockPos lookingAtBlock = PlayerUtils.getPlayerLookingAtBlock(player, false);
        if(world.getBlockState(lookingAtBlock).getBlock() instanceof SaplingBlock){
            if(world.getRegistryKey() == ClientWorld.OVERWORLD){
                int scoreToAdd =0;
                if(player.isSprinting())
                    scoreToAdd=5;
                else if(player.isSneaking())
                    scoreToAdd=1;
                else if(player.isFallFlying())
                    scoreToAdd=4;
                if(scoreToAdd>0){
                    treeScore+=scoreToAdd;
                    player.sendMessage(new LiteralText("树苗生长进度"+ treeScore*5 +"/"+requireScore*5),true);
                    BoneMealItem.createParticles(world,lookingAtBlock,scoreToAdd*5);
                }
                if(treeScore>200){
                    NetworkUtils.sendPacketC2S(FAST_TREE_NETWORK,lookingAtBlock.asLong()+"");
                    treeScore=0;
                }
            }
        }
    }
}
