package calebxzhou.rdi.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * Created  on 2022-11-04,10:42.
 */
@Mixin(MinecraftServer.class)
public class mAlwaysOfflineMode {
	@Overwrite
	public boolean usesAuthentication() {
		return false;
	}
	}
