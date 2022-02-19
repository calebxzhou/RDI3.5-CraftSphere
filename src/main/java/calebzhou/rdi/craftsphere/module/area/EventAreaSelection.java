package calebzhou.rdi.craftsphere.module.area;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class EventAreaSelection {
    public EventAreaSelection() {
        AttackBlockCallback.EVENT.register(((player, world, hand, pos, direction) -> {
            BlockState blockState = world.getBlockState(pos);
            Block block = blockState.getBlock();
            String pid = player.getUuidAsString();


            if(player.getMainHandStack().getItem() == Items.GOLDEN_HOE){
                handleAreaSelection(pos,true);
            }

            return ActionResult.PASS;
        }));
        //右键单击方块事件
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            BlockPos blockPos = hitResult.getBlockPos();
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();
            String pid = player.getUuidAsString();


            if(player.getMainHandStack().getItem() == Items.GOLDEN_HOE){
                handleAreaSelection(blockPos,false);
            }
            return ActionResult.PASS;
        });
    }
    /**
     * @param left_right 左手点1 true 右手点2 false
     * */
    private void handleAreaSelection(BlockPos blockPos,boolean left_right){
        if(left_right){
            //左手
            ModelAreaSelection.INSTANCE.setP1(blockPos);
        }else{
            //右手
            ModelAreaSelection.INSTANCE.setP2(blockPos);
        }
    }
}
