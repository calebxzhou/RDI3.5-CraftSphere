package calebzhou.rdi.core.client.mixin;

import calebzhou.rdi.core.client.loader.LoadProgressDisplay;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelBakery.class)
public class mFinishLoad {
    @Inject(method = "uploadTextures",at=@At("TAIL"))
    private void finishLoad(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir){
        LoadProgressDisplay.onFinish();
    }
}
