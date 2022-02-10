package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.Updater;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinUpdater {
    @Inject(method = "Lnet/minecraft/client/main/Main;main([Ljava/lang/String;)V",remap = false,at=@At("HEAD"))
    private static void upd(String[] args, CallbackInfo ci){
        Updater.check();
    }
}
