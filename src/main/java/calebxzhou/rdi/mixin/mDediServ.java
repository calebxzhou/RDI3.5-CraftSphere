package calebxzhou.rdi.mixin;

import calebxzhou.rdi.RdiCore;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created  on 2023-04-13,22:15.
 */
@Mixin(MinecraftServer.class)
public class mDediServ {
	/*@Inject(method = "createLevels",at=@At("HEAD"), cancellable = true)
	public void createLevels(ChunkProgressListener worldGenerationProgressListener, CallbackInfo ci) {
		RdiCore.getLogger().info("不加载逻辑服务器的地图");
		ci.cancel();
	}*/
}
