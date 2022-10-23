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

@Mixin(Main.class)
public class mClientStartup {

    /*@Redirect(method = "<clinit>",at = @At(value = "INVOKE",target = "Ljava/lang/System;setProperty(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
    private static String headlessNo(String key, String value){
		//是windows就false 不是就true 防止卡死
        String boolstr = Boolean.toString(Util.getPlatform() != Util.OS.WINDOWS);
        System.setProperty("java.awt.headless", boolstr);
		return boolstr;
    }*/
	@Inject(method = "run",at = @At("HEAD"))
	private static void rdiStart(String[] args, boolean enableDataFixerOptimizations, CallbackInfo ci){
		RdiLoader.INSTANCE.onMinecraftStart();
	}


}
