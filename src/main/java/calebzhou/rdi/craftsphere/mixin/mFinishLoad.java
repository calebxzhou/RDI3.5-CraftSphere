package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.misc.LoadProgressDisplay;
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
        LoadProgressDisplay.onFinish();
    }
}
