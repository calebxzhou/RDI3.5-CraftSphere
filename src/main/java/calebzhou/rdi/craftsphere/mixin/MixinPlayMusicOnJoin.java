package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.module.GameMusic;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public class MixinPlayMusicOnJoin {
    @Inject(method = "Lnet/minecraft/client/world/ClientWorld;addPlayer(ILnet/minecraft/client/network/AbstractClientPlayerEntity;)V"
            ,at = @At("TAIL"))
    private void qseda(CallbackInfo ci){
        GameMusic.play();
    }
}
