package calebxzhou.rdi.mixin;

import calebxzhou.rdi.RdiLoader;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class mClientStartup {


	@Inject(method = "run",at = @At("HEAD"))
	private static void rdiStart(String[] args, boolean enableDataFixerOptimizations, CallbackInfo ci){
		RdiLoader.onMinecraftStart(args);
	}


}
