package calebxzhou.rdi.mixin;

import com.google.gson.JsonObject;
import net.minecraft.DetectedVersion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static calebxzhou.rdi.consts.RdiConsts.PROTOCOL_VERSION;

@Mixin(DetectedVersion.class)
public class mProtocol {
    @Shadow
    @Final
    @Mutable
    private int protocolVersion;
    @Inject(
            method = "<init>(Lcom/google/gson/JsonObject;)V",
            at=@At("TAIL")
    )
    private void changeProtocolVersion(JsonObject json, CallbackInfo ci){
        protocolVersion= PROTOCOL_VERSION;
    }
}
