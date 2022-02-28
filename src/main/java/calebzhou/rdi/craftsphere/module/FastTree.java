package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.SaplingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.BoneMealItem;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.Optional;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class FastTree implements WorldTickable {
    public static final Identifier FAST_TREE_NETWORK =new Identifier(MODID,"fast_tree");
    private int treeScore =0;
    //生长树苗需要200
    private final int requireScore =500;
    public FastTree() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
    }
    @Override
    public void tickWorld(ClientWorld world) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        Optional<BlockPos> nearestSaplings = getNearestSaplings(player.getLandingPos(),world);
        if(world.getRegistryKey() == ClientWorld.OVERWORLD && nearestSaplings.isPresent()){
            handleGrowTree(player,world,nearestSaplings.get());
        }

    }
    private void handleGrowTree(ClientPlayerEntity player, ClientWorld world, BlockPos nearestSaplings){

        int scoreToAdd =0;
        if(player.isSprinting())
            scoreToAdd=3;
        else if(player.isFallFlying())
            scoreToAdd=2;
        else if(player.isSneaking())
            scoreToAdd=1;
        if(scoreToAdd>0){
            treeScore+=scoreToAdd;
            player.sendMessage(new LiteralText("树苗生长进度"+ treeScore*5 +"/"+requireScore*5),true);
            final int finalScoreToAdd = scoreToAdd;
                BoneMealItem.createParticles(world,nearestSaplings, finalScoreToAdd *5);

        }
        if(treeScore>requireScore){
                NetworkUtils.sendPacketC2S(FAST_TREE_NETWORK,nearestSaplings.asLong()+"");

            treeScore=0;
        }
    }

    private Optional<BlockPos> getNearestSaplings(BlockPos playerPos, ClientWorld world){
        return BlockPos.stream(playerPos.add(-5, -2, -5), playerPos.add(5, 2, 5))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof SaplingBlock)
                .findFirst()
                .map(BlockPos::toImmutable);
    }

}
