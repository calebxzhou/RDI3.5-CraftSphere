package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.util.ThreadPool;
import calebzhou.rdi.craftsphere.module.UpdateChecker;
import net.minecraft.client.main.Main;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Main.class)
public class MixinUpdateChecker {
    @Inject(method = "Lnet/minecraft/client/main/Main;main([Ljava/lang/String;)V",remap = false,at=@At("HEAD"))
    private static void upd(String[] args, CallbackInfo ci){
        ThreadPool.newThread(()->{
            UpdateChecker.check();
        });

    }
}
