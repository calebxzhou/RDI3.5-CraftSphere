package calebzhou.rdi.core.client.mixin.network;

import calebzhou.rdi.core.client.network.UdpChannel;
import calebzhou.rdi.core.client.network.UdpServerChannel;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.network.*;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.LegacyQueryHandler;
import net.minecraft.server.network.ServerConnectionListener;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import net.minecraft.util.LazyLoadedValue;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.List;

/**
 * Created  on 2022-11-04,8:44.
 */
@Mixin(Connection.class)
public class mConnection {
	@Overwrite
	public static Connection connectToServer(InetSocketAddress address, boolean useEpollIfAvailable) {
		final Connection connection = new Connection(PacketFlow.CLIENTBOUND);
		new Bootstrap()
				.group(Connection.NETWORK_WORKER_GROUP.get())
				.handler(
						new ChannelInitializer<Channel>() {
							@Override
							protected void initChannel(Channel channel) {
								try {
									channel.config().setOption(ChannelOption.SO_BROADCAST, true);
								} catch (ChannelException var3) {
								}

								channel.pipeline()
										.addLast("timeout", new ReadTimeoutHandler(30))
										.addLast("splitter", new Varint21FrameDecoder())
										.addLast("decoder", new PacketDecoder(PacketFlow.CLIENTBOUND))
										.addLast("prepender", new Varint21LengthFieldPrepender())
										.addLast("encoder", new PacketEncoder(PacketFlow.SERVERBOUND))
										.addLast("packet_handler", connection);
							}
						}
				)
				.channel(UdpChannel.class)
				.connect(address.getAddress(), address.getPort())
				.syncUninterruptibly();
		return connection;
	}
}

@Mixin(ServerConnectionListener.class)
class mServerConnectionListener {
	@Shadow
	@Final
	private List<ChannelFuture> channels;

	@Shadow
	@Final
	MinecraftServer server;

	@Shadow
	@Final
	List<Connection> connections;

	@Overwrite
	public void startTcpServerListener(@Nullable InetAddress address, int port) {
		synchronized (channels) {
			ServerConnectionListener listener = (ServerConnectionListener)(Object) this;
			this.channels
					.add(
							new ServerBootstrap()
									.channel(UdpServerChannel.class)
									.childHandler(
											new ChannelInitializer<Channel>() {
												@Override
												protected void initChannel(Channel channel) {
													try {
														channel.config().setOption(ChannelOption.SO_BROADCAST, true);
													} catch (ChannelException var4) {
													}

													channel.pipeline()
															.addLast("timeout", new ReadTimeoutHandler(30))
															.addLast("legacy_query", new LegacyQueryHandler(listener))
															.addLast("splitter", new Varint21FrameDecoder())
															.addLast("decoder", new PacketDecoder(PacketFlow.SERVERBOUND))
															.addLast("prepender", new Varint21LengthFieldPrepender())
															.addLast("encoder", new PacketEncoder(PacketFlow.CLIENTBOUND));
													int i = server.getRateLimitPacketsPerSecond();
													Connection connection = (Connection) (i > 0 ? new RateKickingConnection(i) : new Connection(PacketFlow.SERVERBOUND));
													connections.add(connection);
													channel.pipeline().addLast("packet_handler", connection);
													connection.setListener(new ServerHandshakePacketListenerImpl(server, connection));
												}
											}
									)
									.group(ServerConnectionListener.SERVER_EVENT_GROUP.get())
									.localAddress(address, port)
									.bind()
									.syncUninterruptibly()
					);
		}
	}
}
