package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.misc.LoadProgressDisplay;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.client.renderer.texture.AtlasSet;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(ModelBakery.class)
public class mFinishLoad {
    @Inject(method = "uploadTextures",at=@At("TAIL"))
    private void finishLoad(TextureManager textureManager, ProfilerFiller profilerFiller, CallbackInfoReturnable<AtlasSet> cir){
        LoadProgressDisplay.onFinish();
    }
}
