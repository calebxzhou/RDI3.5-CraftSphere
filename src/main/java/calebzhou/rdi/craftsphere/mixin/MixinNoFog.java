package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public class MixinNoFog {
    /**
     * @author 没有雾！
     */
    @Inject(method = "Lnet/minecraft/client/renderer/FogRenderer;setupFog(Lnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/FogRenderer$FogMode;FZF)V",
    at = @At("HEAD"), cancellable = true)
    private static void noFogdebug(Camera camera, FogRenderer.FogMode fogMode, float f, boolean bl, float g, CallbackInfo ci){
        if(ExampleMod.debug)
            ci.cancel();
    }


}
