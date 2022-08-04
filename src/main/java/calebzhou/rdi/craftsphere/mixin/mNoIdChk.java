package calebzhou.rdi.craftsphere.mixin;


import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.RemapException;
import net.fabricmc.fabric.impl.registry.sync.RemappableRegistry;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistryPacketHandler;
import net.minecraft.core.Registry;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.thread.BlockableEventLoop;
import net.minecraft.world.item.crafting.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@Mixin(RegistrySyncManager.class)
public abstract class mNoIdChk {
    @Shadow
    public static void apply(Map<ResourceLocation, Object2IntMap<ResourceLocation>> map, RemappableRegistry.RemapMode mode) throws RemapException {
    }

    @Overwrite
    public static void receivePacket(BlockableEventLoop<?> executor, RegistryPacketHandler handler, FriendlyByteBuf buf, boolean accept, Consumer<Exception> errorHandler) {
        handler.receivePacket(buf);
        if (!handler.isPacketFinished()) {
            return;
        }

        /*if (DEBUG) {
            String handlerName = handler.getClass().getSimpleName();
            LOGGER.info("{} total packet: {}", handlerName, handler.getTotalPacketReceived());
            LOGGER.info("{} raw size: {}", handlerName, handler.getRawBufSize());
            LOGGER.info("{} deflated size: {}", handlerName, handler.getDeflatedBufSize());
        }*/

        Map<ResourceLocation, Object2IntMap<ResourceLocation>> map = handler.getSyncedRegistryMap();

        if (accept) {
            try {
                executor.submit(() -> {
                    if (map == null) {
                        //errorHandler.accept(new RemapException("Received null map in sync packet!"));
                        return null;
                    }

                    try {
                        apply(map, RemappableRegistry.RemapMode.REMOTE);
                    } catch (RemapException e) {
                       // errorHandler.accept(e);
                    }

                    return null;
                }).get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
               // errorHandler.accept(e);
            }
        }
    }

}
@Mixin(ClientboundUpdateRecipesPacket.class)
class chk2{
    @Overwrite
    public static Recipe<?> fromNetwork(FriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
        try {
            return Registry.RECIPE_SERIALIZER.getOptional(resourceLocation).get()/*.orElseThrow(() -> new IllegalArgumentException("Unknown recipe serializer " + resourceLocation))*/.fromNetwork(resourceLocation2, friendlyByteBuf);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        return null;
    }
}
@Mixin(Connection.class)
abstract
class chk3{
    @Shadow public abstract void send(Packet<?> packet);

    @Inject(method = "exceptionCaught",at = @At("HEAD"), cancellable = true)
    public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo ci) {
        ci.cancel();
    }
}