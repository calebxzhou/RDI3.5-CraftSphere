package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import com.google.gson.JsonObject;
import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftVersion.class)
public class MixinProtocol {
    /**
     * @author
     */
    @Shadow
    @Final
    @Mutable
    private int protocolVersion;
    @Shadow @Final @Mutable
    private String name;
    @Inject(
            method = "Lnet/minecraft/MinecraftVersion;<init>(Lcom/google/gson/JsonObject;)V",
            at=@At("TAIL")
    )
    private void asdasdcx(JsonObject json, CallbackInfo ci){
        protocolVersion=ExampleMod.VERSION;
    }
}
