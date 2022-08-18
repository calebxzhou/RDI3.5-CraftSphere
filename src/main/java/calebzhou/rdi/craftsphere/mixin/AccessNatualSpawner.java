package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NaturalSpawner.class)
public interface AccessNatualSpawner {
    @Accessor
    MobCategory[] getSPAWNING_CATEGORIES();
}
