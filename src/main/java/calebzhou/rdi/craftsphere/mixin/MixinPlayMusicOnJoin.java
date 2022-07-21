package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.module.GameMusic;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class MixinPlayMusicOnJoin {
    @Inject(method = "Lnet/minecraft/client/multiplayer/ClientLevel;addPlayer(ILnet/minecraft/client/player/AbstractClientPlayer;)V"
            ,at = @At("TAIL"))
    private void qseda(CallbackInfo ci){
        GameMusic.playInGame();
    }
}
