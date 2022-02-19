package calebzhou.rdi.craftsphere.module.area.mixin;

import calebzhou.rdi.craftsphere.module.area.RendererAreaSelection;
import calebzhou.rdi.craftsphere.module.area.ModelAreaSelection;
import calebzhou.rdi.craftsphere.util.DialogUtils;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(WorldRenderer.class)
public class MixinSelectionRenderer {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;FJZLnet/minecraft/client/render/Camera;Lnet/minecraft/client/render/GameRenderer;Lnet/minecraft/client/render/LightmapTextureManager;Lnet/minecraft/util/math/Matrix4f;)V",
    at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/debug/DebugRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider$Immediate;DDD)V"),
    locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private void qaess(MatrixStack matrices, float tickDelta, long limitTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f positionMatrix, CallbackInfo ci){
        if(!ModelAreaSelection.displayArea) return;
        ModelAreaSelection mas = ModelAreaSelection.INSTANCE;
        if(mas.getP1().isEmpty() || mas.getP2().isEmpty()){
            DialogUtils.showInfoIngame("请选择两个区域点。");
            ModelAreaSelection.displayArea=false;
            return;
        }
        Vec3d vec3d = camera.getPos();
        double d = vec3d.getX();
        double e = vec3d.getY();
        double f = vec3d.getZ();
        VertexConsumerProvider.Immediate immediate = bufferBuilders.getEntityVertexConsumers();

        RendererAreaSelection.INSTANCE.render(matrices,immediate,d,e,f);

    }
}
