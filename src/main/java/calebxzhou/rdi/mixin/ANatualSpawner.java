package calebxzhou.rdi.mixin;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(NaturalSpawner.class)
public interface ANatualSpawner {
    @Accessor
    MobCategory[] getSPAWNING_CATEGORIES();
}
