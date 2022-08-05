package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import calebzhou.rdi.craftsphere.misc.LoadFinishHandler;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AdvancementList.class)
public class mFinishLoad {
    @Inject(method = "add",at=@At("TAIL"))
    private void finishLoad(Map<ResourceLocation, Advancement.Builder> map, CallbackInfo ci){
        LoadFinishHandler.handle();
    }
}
