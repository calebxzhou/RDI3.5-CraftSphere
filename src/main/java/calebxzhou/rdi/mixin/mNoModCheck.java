package calebxzhou.rdi.mixin;

import calebxzhou.rdi.RdiCore;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.PacketDecoder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.profiling.jfr.JvmProfiler;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;
import org.quiltmc.qsl.registry.impl.sync.client.ClientRegistrySync;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.DataInput;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * Created  on 2023-04-09,21:39.
 */
@Mixin(ClientRegistrySync.class)
public class mNoModCheck {
	//@Inject(method = "checkMissing",at=@At("HEAD"), cancellable = true)
	private static void mis(ClientPacketListener handler, ResourceLocation registry, Collection<SynchronizedRegistry.MissingEntry> missingEntries, CallbackInfoReturnable<Boolean> cir){
		cir.setReturnValue(false);
	}
}
@Mixin(NbtIo.class)
class mAntiTagCrash{
	//@Redirect(method = "readUnnamedTag",at=@At(value = "INVOKE",target = "Lnet/minecraft/nbt/TagType;load(Ljava/io/DataInput;ILnet/minecraft/nbt/NbtAccounter;)Lnet/minecraft/nbt/Tag;"))
	private static Tag dgvh(TagType instance, DataInput dataInput, int depth, NbtAccounter accounter){
		try {
			return TagTypes.getType(dataInput.readByte()).load(dataInput, depth, accounter);
		} catch (Exception e) {
			RdiCore.getLogger().error("读取NBT错误：{}",e.toString());
		}
		return null;
	}
}
@Mixin(ResourceLocation.class)
class mNoIdentifierCheck{
	//@Overwrite
	private static boolean isValidPath(String path) {
		return true;
	}
	//@Overwrite
	private static boolean isValidNamespace(String namespace) {
		return true;
	}
}
@Mixin(PacketDecoder.class)
class mNewPackDecoder extends ByteToMessageDecoder {
	@Shadow
	@Final
	private PacketFlow flow;

	@Shadow
	@Final
	private static Logger LOGGER;

	//@Overwrite
	public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
		try {
			int i = byteBuf.readableBytes();
			if (i == 0) {
				return;
			}
			FriendlyByteBuf friendlyByteBuf = new FriendlyByteBuf(byteBuf);
			int j = friendlyByteBuf.readVarInt();
			Packet<?> packet = ((ConnectionProtocol)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get())
					.createPacket(flow, j, friendlyByteBuf);
			if (packet != null) {
				int k = ((ConnectionProtocol)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get()).getId();
				JvmProfiler.INSTANCE.onPacketReceived(k, j, channelHandlerContext.channel().remoteAddress(), i);
				if (friendlyByteBuf.readableBytes() > 0) {
					LOGGER.warn(
							"Packet "
									+ ((ConnectionProtocol)channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get()).getId()
									+ "/"
									+ j
									+ " ("
									+ packet.getClass().getSimpleName()
									+ ") was larger than I expected, found "
									+ friendlyByteBuf.readableBytes()
									+ " bytes extra whilst reading packet "
									+ j
					);
				} else {
					list.add(packet);
						LOGGER.debug(
								Connection.PACKET_RECEIVED_MARKER,
								" IN: [{}:{}] {}",
								channelHandlerContext.channel().attr(Connection.ATTRIBUTE_PROTOCOL).get(),
								j,
								packet.getClass().getName()
						);

				}
			} else {
				LOGGER.warn("Bad packet id " + j);
			}
		} catch (Exception e) {
			RdiCore.getLogger().error("读取数据包错误：{}",e.toString());
		}
	}
}
