package calebzhou.rdi.craftsphere.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

public class PlayerUtils {
    public static BlockPos getPlayerLookingAtBlock(Player player, boolean isFluid){
        BlockHitResult rays=(BlockHitResult) player.pick(256.0D,0.0f,isFluid);
        return rays.getBlockPos();
    }
}
