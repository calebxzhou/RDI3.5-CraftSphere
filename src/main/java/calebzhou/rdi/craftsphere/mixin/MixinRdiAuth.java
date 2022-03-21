package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.ExampleMod;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.User;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.UUID;

@Mixin(User.class)
public class MixinRdiAuth {
    @Shadow @Final @Mutable private String uuid;

    @Shadow @Final private String name;

    /**
     * @author
     */
    @Overwrite
    public GameProfile getGameProfile() {
        return new GameProfile(UUID.fromString(ExampleMod.UID),name);
    }
    /**
     * @author
     */
    @Overwrite
    public String getUuid() {
        return ExampleMod.UID;
    }
    @Inject(method = "<init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/Optional;Ljava/util/Optional;Lnet/minecraft/client/User$Type;)V",
    at = @At("TAIL"))
    private void setUuid(String string, String string2, String string3, Optional optional, Optional optional2, User.Type type, CallbackInfo ci){
        uuid=ExampleMod.UID;
    }
}
@Mixin(ServerboundHelloPacket.class)
class MixinRdiAuth2{
    @Mutable
    @Shadow @Final private GameProfile gameProfile;

    @Inject(method = "Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;<init>(Lcom/mojang/authlib/GameProfile;)V",
    at = @At("TAIL"))
    private void setUuid(GameProfile gameProfile, CallbackInfo ci){
        this.gameProfile=new GameProfile(ExampleMod._UUID,gameProfile.getName());
    }
    @Inject(method = "Lnet/minecraft/network/protocol/login/ServerboundHelloPacket;<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",
            at = @At("TAIL"))
    private void setUuid2(FriendlyByteBuf friendlyByteBuf, CallbackInfo ci){
        this.gameProfile=new GameProfile(ExampleMod._UUID,gameProfile.getName());
    }
}