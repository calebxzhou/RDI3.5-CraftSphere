package calebzhou.rdi.craftsphere.mixin;


import calebzhou.rdi.craftsphere.UserInfoStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Optional;

@Mixin(ServerboundHelloPacket.class)
public class mLoginProtocol {
    @Shadow @Final private String name;
    @Shadow @Final private Optional<ProfilePublicKey.Data> publicKey;
    private static final int nameLen=64;//给uuid和@符号腾出来空间

    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",constant =
    @Constant(intValue = 16))
    private static int changeNameLength(int constant){
        return nameLen;
    }
    @Overwrite
    public void write(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(name+"@"+UserInfoStorage.UserUuid, nameLen);
        friendlyByteBuf.writeOptional(publicKey, (friendlyByteBuf2, data) -> data.write(friendlyByteBuf));
    }

}
