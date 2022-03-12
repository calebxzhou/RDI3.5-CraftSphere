package calebzhou.rdi.craftsphere.module;

import calebzhou.rdi.craftsphere.util.NetworkUtils;
import calebzhou.rdi.craftsphere.util.WorldTickable;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.level.block.SaplingBlock;
import java.util.Optional;

import static calebzhou.rdi.craftsphere.ExampleMod.MODID;

public class FastTree implements WorldTickable {
    public static final ResourceLocation FAST_TREE_NETWORK =new ResourceLocation(MODID,"fast_tree");
    private int treeScore =0;
    //生长树苗需要200
    private final int requireScore =500;
    public FastTree() {
        ClientTickEvents.END_WORLD_TICK.register(this::tickWorld);
    }
    @Override
    public void tickWorld(ClientLevel world) {
        LocalPlayer player = Minecraft.getInstance().player;
        Optional<BlockPos> nearestSaplings = getNearestSaplings(player.getOnPos(),world);
        if(world.dimension() == ClientLevel.OVERWORLD && nearestSaplings.isPresent()){
            handleGrowTree(player,world,nearestSaplings.get());
        }

    }
    private void handleGrowTree(LocalPlayer player, ClientLevel world, BlockPos nearestSaplings){

        int scoreToAdd =0;
        if(player.isSprinting())
            scoreToAdd=3;
        else if(player.isFallFlying())
            scoreToAdd=2;
        else if(player.isShiftKeyDown())
            scoreToAdd=1;
        if(scoreToAdd>0){
            treeScore+=scoreToAdd;
            player.displayClientMessage(new TextComponent("树苗生长进度"+ treeScore*5 +"/"+requireScore*5),true);
            final int finalScoreToAdd = scoreToAdd;
                BoneMealItem.addGrowthParticles(world,nearestSaplings, finalScoreToAdd *5);

        }
        if(treeScore>requireScore){
                NetworkUtils.sendPacketC2S(FAST_TREE_NETWORK,nearestSaplings.asLong()+"");

            treeScore=0;
        }
    }

    private Optional<BlockPos> getNearestSaplings(BlockPos playerPos, ClientLevel world){
        return BlockPos.betweenClosedStream(playerPos.offset(-5, -2, -5), playerPos.offset(5, 2, 5))
                .filter(blockPos -> world.getBlockState(blockPos).getBlock() instanceof SaplingBlock)
                .findFirst()
                .map(BlockPos::immutable);
    }

}
