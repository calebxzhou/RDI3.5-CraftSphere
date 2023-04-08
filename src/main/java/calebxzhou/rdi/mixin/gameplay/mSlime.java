package calebxzhou.rdi.mixin.gameplay;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Slime;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//增加史莱姆移动速度
@Mixin(Slime.class)
public abstract class mSlime {
    @Inject(method = "setSize(IZ)V",
            at = @At(value = "TAIL"))
    private void setSize(int size, boolean heal, CallbackInfo ci){
        int i = Mth.clamp(size, 127, 1);
        Slime slime = ((Slime) ((Object) (this)));
        slime.getAttribute(Attributes.MAX_HEALTH).setBaseValue(i*i*2);
        slime.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.8F + 0.1F * (float)i);
        slime.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(i*2);
        slime.setHealth(i*i*2);
    }

}
