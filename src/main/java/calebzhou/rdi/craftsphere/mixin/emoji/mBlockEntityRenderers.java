package calebzhou.rdi.craftsphere.mixin.emoji;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderers.class)
public abstract class mBlockEntityRenderers {

    @Shadow
    private static <T extends BlockEntity> void register(BlockEntityType<? extends T> blockEntityType, BlockEntityRendererProvider<T> blockEntityRendererProvider) {
    }

    @Inject(method = "<clinit>",at = @At("TAIL"))
    private static void rdi_registerSignEmojiRenderer(CallbackInfo ci){
        register(BlockEntityType.SIGN, context -> {
            SignRenderer signRenderer = new SignRenderer(context);
            return signRenderer;
        });
    }
}
