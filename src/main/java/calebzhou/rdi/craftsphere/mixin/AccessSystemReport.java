package calebzhou.rdi.craftsphere.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.SystemReport;

@Mixin(SystemReport.class)
public interface AccessSystemReport {
    @Accessor
    Map<String, String> getEntries();
}
