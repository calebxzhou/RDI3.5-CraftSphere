package calebzhou.rdi.core.client.mixin.gameplay;

import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Witch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

//女巫增强
@Mixin(Witch.class)
public class MixinWitch {
    /**
     * @author
     */
    @Overwrite
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.MOVEMENT_SPEED, 0.5);
    }
    //缩短攻击间隔
    @ModifyConstant(method = "Lnet/minecraft/world/entity/monster/Witch;registerGoals()V",
            constant = @Constant(intValue = 60))
    private int intervalMinus(int constant){
        return 5;
    }
}
