package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import com.google.gson.JsonObject;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DetectedVersion.class)
public class MixinProtocol {
    @Shadow
    @Final
    @Mutable
    private int protocolVersion;
    @Shadow @Final @Mutable
    private String name;
    @Inject(
            method = "Lnet/minecraft/DetectedVersion;<init>(Lcom/google/gson/JsonObject;)V",
            at=@At("TAIL")
    )
    private void changeProtocolVersion(JsonObject json, CallbackInfo ci){
        protocolVersion=ExampleMod.VERSION;
    }
}
