package calebzhou.rdi.craftsphere.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class PlayerUtils {
    public static BlockPos getPlayerLookingAtBlock(PlayerEntity player, boolean isFluid){
        BlockHitResult rays=(BlockHitResult) player.raycast(256.0D,0.0f,isFluid);
        return rays.getBlockPos();
    }
}
