package calebzhou.rdi.core.client.mixin;

import net.minecraft.SystemReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(SystemReport.class)
public interface AccessSystemReport {
    @Accessor
    Map<String, String> getEntries();
}
