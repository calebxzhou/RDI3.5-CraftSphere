package calebxzhou.rdi

import net.minecraft.nbt.CompoundTag
import net.minecraft.resources.ResourceLocation
import org.quiltmc.qsl.networking.api.PacketByteBufs
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking

/**
 * Created  on 2023-04-07,23:11.
 */
object NetTx {
    @JvmStatic
    fun <T> send(packId: ResourceLocation, content: T){
        val buf = PacketByteBufs.create()
        when (content) {
            is Int -> buf.writeInt(content)
            is Double -> buf.writeDouble(content)
            is Float -> buf.writeFloat(content)
            is Long -> buf.writeLong(content)
            is CompoundTag -> buf.writeNbt(content)
            else -> buf.writeUtf(content.toString())
        }
        try {
            ClientPlayNetworking.send(packId, buf)
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }
}
