package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
//不允许亮度夜视
@Mixin(Options.class)
public class MixinLuminChk {
    @Final
    @Shadow
    private OptionInstance<Double> gamma;

    @Redirect(method = "processOptions(Lnet/minecraft/client/Options$FieldAccess;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/world/entity/player/PlayerModelPart;values()[Lnet/minecraft/world/entity/player/PlayerModelPart;"))
    private PlayerModelPart[] efACdaew(){
        //如果亮度过高，并且不是调试模式，就会sleep
        if(gamma.get()>1.0 && !ExampleMod.debug){
            try {
                System.out.println("1");
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return PlayerModelPart.values();
    }
}
