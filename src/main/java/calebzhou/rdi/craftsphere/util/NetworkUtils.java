package calebzhou.rdi.craftsphere.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NetworkUtils {
    public static void sendPacketC2S(ResourceLocation packType, String content){
        FriendlyByteBuf buf = PacketByteBufs.create();
        buf.writeUtf(content);
        ClientPlayNetworking.send(packType,buf);
    }


}
