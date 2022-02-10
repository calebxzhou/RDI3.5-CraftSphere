package calebzhou.rdi.craftsphere.mixin;

import net.minecraft.util.SystemDetails;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SystemDetails.class)
public interface AccessSystemDetails {
    @Accessor
    Map<String, String> getSections();
}
