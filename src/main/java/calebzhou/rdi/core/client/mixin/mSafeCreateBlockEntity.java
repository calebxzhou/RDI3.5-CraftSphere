package calebzhou.rdi.core.client.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Created  on 2022-10-28,16:54.
 */
@Mixin(LevelChunk.class)
public abstract class mSafeCreateBlockEntity {
	@Shadow
	public abstract BlockState getBlockState(BlockPos pos);

	@Overwrite
	private @Nullable BlockEntity createBlockEntity(BlockPos pos) {
		try {
			BlockState blockState = getBlockState(pos);
			return !blockState.hasBlockEntity() ? null : ((EntityBlock)blockState.getBlock()).newBlockEntity(pos, blockState);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
@Mixin(Level.class)
abstract
class mSafeCreateBlockEntity2 implements LevelHeightAccessor {
	@Shadow
	@Final
	public boolean isClientSide;

	@Shadow
	@Final
	private Thread thread;

	@Shadow
	public abstract LevelChunk getChunkAt(BlockPos pos);

	@Overwrite
	public @Nullable BlockEntity getBlockEntity(BlockPos pos) {
		try {
			if (isOutsideBuildHeight(pos)) {
				return null;
			} else {
				return !isClientSide && Thread.currentThread() != thread
						? null
						: getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.IMMEDIATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
