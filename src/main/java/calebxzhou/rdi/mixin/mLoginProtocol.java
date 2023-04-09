package calebxzhou.rdi.mixin;


import calebxzhou.libertorch.SerDesJson;
import calebxzhou.rdi.RdiCore;
import calebxzhou.rdi.model.RdiUser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.Optional;
import java.util.UUID;

@Mixin(ServerboundHelloPacket.class)
public class mLoginProtocol {
    @Shadow @Final private String name;
    @Shadow @Final private Optional<ProfilePublicKey.Data> publicKey;
	@Shadow
	@Final
	private Optional<UUID> profileId;
	private static final int nameLen=128;//给uuid和@符号腾出来空间

    @ModifyConstant(method = "<init>(Lnet/minecraft/network/FriendlyByteBuf;)V",constant =
    @Constant(intValue = 16))
    private static int changeNameLength(int constant){
        return nameLen;
    }
    @Overwrite
    public void write(FriendlyByteBuf friendlyByteBuf) {
		//改变登录格式
        friendlyByteBuf.writeUtf(SerDesJson.gson.toJson(RdiUser.now), nameLen);
        friendlyByteBuf.writeOptional(publicKey, (friendlyByteBuf2, data) -> data.write(friendlyByteBuf));
		friendlyByteBuf.writeOptional(profileId, FriendlyByteBuf::writeUUID);
    }

}
