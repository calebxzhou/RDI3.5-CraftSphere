package calebzhou.rdi.craftsphere.util;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class NetworkUtils {
    public static void sendPacketToServer(ResourceLocation packType, Object content){
        FriendlyByteBuf buf = PacketByteBufs.create();
        if(content instanceof Integer i)
            buf.writeInt(i);
        else if(content instanceof Double i)
            buf.writeDouble(i);
        else if(content instanceof Float i)
            buf.writeFloat(i);
        else if(content instanceof Long i)
            buf.writeLong(i);
        else if(content instanceof CompoundTag i)
            buf.writeNbt(i);
        else
            buf.writeUtf(content+"");
        ClientPlayNetworking.send(packType,buf);
    }


}
