package com.xuzhong.rpc.test.clientFactory;

import java.net.InetSocketAddress;

import com.xuzhong.rpc.test.clientFactory.pojo.RemotingUrl;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SystemPropertyUtil;

public class NettyClientFactory extends AbstractClientFactory {

	public static final NioEventLoopGroup workerGroup = new NioEventLoopGroup();

	public static final ByteBufAllocator byteBufAllocator;

	static {
		workerGroup.setIoRatio(SystemPropertyUtil.getInt("ioratio", 100));

		if (SystemPropertyUtil.getBoolean("bytebuf.pool", false)) {
			byteBufAllocator = PooledByteBufAllocator.DEFAULT;
		} else {
			byteBufAllocator = UnpooledByteBufAllocator.DEFAULT;
		}
	}

	@Override
	protected Client createClient(RemotingUrl url) throws Exception {
		final Bootstrap bootstrap = new Bootstrap();

		bootstrap.group(NettyClientFactory.workerGroup)//
				.option(ChannelOption.TCP_NODELAY, true)//
				.option(ChannelOption.SO_REUSEADDR, true)//
				.option(ChannelOption.SO_KEEPALIVE, false)//
				.option(ChannelOption.ALLOCATOR, NettyClientFactory.byteBufAllocator)//
				.channel(NioSocketChannel.class)//
				.handler(new ChannelInitializer<NioSocketChannel>() {

					@Override
					protected void initChannel(NioSocketChannel ch) throws Exception {
						ch.pipeline().addLast("decoder", null)//
								.addLast("encoder", null)//
								.addLast("handler", null);

					}

				});

		int connectTimeout = url.getConnectionTimeout();

		if (connectTimeout < 1000) {
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000);
		} else {
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout);
		}

		String targetIP = url.getDomain();
		int targetPort = url.getPort();

		ChannelFuture future = bootstrap.connect(new InetSocketAddress(targetIP, targetPort));

		if (future.awaitUninterruptibly(connectTimeout) && future.isSuccess() && future.channel().isActive()) {
            Channel channel = future.channel();
            Client client = new NettyClient(url, channel);
            return client;
        } else {
            future.cancel(true);
            future.channel().close();
            throw new Exception(targetIP);
        }
		
		
	}

}
