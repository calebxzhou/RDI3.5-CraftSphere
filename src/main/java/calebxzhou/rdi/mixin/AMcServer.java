package calebxzhou.rdi.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Created  on 2023-04-14,8:51.
 */
@Mixin(MinecraftServer.class)
public interface AMcServer {
	@Invoker
	void invokeRunServer();
}
