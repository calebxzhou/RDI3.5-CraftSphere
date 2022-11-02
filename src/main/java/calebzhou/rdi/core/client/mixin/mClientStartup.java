package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.RdiLoader;
import calebzhou.rdi.core.client.loader.LoadProgressRecorder;
import net.minecraft.Util;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Main.class)
public class mClientStartup {


	@Inject(method = "run",at = @At("HEAD"))
	private static void rdiStart(String[] args, boolean enableDataFixerOptimizations, CallbackInfo ci){
		RdiLoader.onMinecraftStart(args);
	}


}
