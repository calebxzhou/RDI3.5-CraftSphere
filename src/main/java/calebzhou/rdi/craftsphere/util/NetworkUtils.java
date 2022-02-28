package calebzhou.rdi.craftsphere.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class NetworkUtils {
    public static void sendPacketC2S(Identifier packType, String content){
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeString(content);
        ClientPlayNetworking.send(packType,buf);
    }


}
