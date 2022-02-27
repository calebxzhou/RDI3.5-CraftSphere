package calebzhou.rdi.craftsphere.module.mobspawn.mixin;

import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.SpawnHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ClientChunkManager.class)
public class MixinClientChunkManager {
    @Shadow @Final private ClientWorld world;

    @Inject(method = "Lnet/minecraft/client/world/ClientChunkManager;tick(Ljava/util/function/BooleanSupplier;)V",at = @At("HEAD"))
    private void tick(BooleanSupplier booleanSupplier, CallbackInfo ci){
        SpawnHelper.Info info;
    }
}
