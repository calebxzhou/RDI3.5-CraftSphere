package calebzhou.rdi.craftsphere.mixin;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.ClientLevelData.class)
public class mSkyAlwaysBlue {
    @Shadow @Final @Mutable  private boolean isFlat;

    @Inject(method = "<init>",at=@At("TAIL"))
    private void asd(Difficulty difficulty, boolean bl, boolean bl2, CallbackInfo ci){
        isFlat=true;
    }
}
