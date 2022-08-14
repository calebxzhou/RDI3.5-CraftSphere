package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//不允许亮度夜视
@Mixin(Options.class)
public abstract class mLuminChk {
    @Final
    @Shadow
    private OptionInstance<Double> gamma;

    @Shadow public abstract OptionInstance<Double> gamma();

    @Inject(method = "processOptions(Lnet/minecraft/client/Options$FieldAccess;)V",
    at = @At("HEAD"))
    private void efACdaew( CallbackInfo ci){
        //如果亮度过高，并且不是调试模式，就会崩
        if(gamma.get()>1.0 && !ExampleMod.debug){
            gamma().set(1.0);
        }

    }
}
