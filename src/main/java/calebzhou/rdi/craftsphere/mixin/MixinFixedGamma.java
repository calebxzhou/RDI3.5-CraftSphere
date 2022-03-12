package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Options.class)
public class MixinFixedGamma {
    @Shadow public double gamma;
//不允许亮度夜视
    @Redirect(method = "Lnet/minecraft/client/Options;processOptions(Lnet/minecraft/client/Options$FieldAccess;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/world/entity/player/PlayerModelPart;values()[Lnet/minecraft/world/entity/player/PlayerModelPart;"))
    private PlayerModelPart[] efACdaew(){
        if(gamma>1.0 && !ExampleMod.debug){
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return PlayerModelPart.values();
    }
}
