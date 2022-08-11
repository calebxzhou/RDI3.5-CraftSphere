package calebzhou.rdi.craftsphere.mixin;

import calebzhou.rdi.craftsphere.NetworkPackets;
import calebzhou.rdi.craftsphere.misc.HwSpec;
import calebzhou.rdi.craftsphere.util.NetworkUtils;
import com.google.gson.Gson;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//发送硬件参数
@Mixin(ClientPacketListener.class)
public class mSendHwSpec {
    @Inject(method = "handleAddPlayer(Lnet/minecraft/network/protocol/game/ClientboundAddPlayerPacket;)V",
    at = @At("TAIL"))
    private void send(ClientboundAddPlayerPacket clientboundAddPlayerPacket, CallbackInfo ci){
        String info = new Gson().toJson(HwSpec.getSystemSpec());
        NetworkUtils.sendPacketToServer(NetworkPackets.HW_SPEC,info);
    }
}
