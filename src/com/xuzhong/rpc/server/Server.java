package com.xuzhong.rpc.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class Server {
	private static final EventLoopGroup bossGroup = new NioEventLoopGroup();

	private static final EventLoopGroup workerGroup = new NioEventLoopGroup();

	private ServerBootstrap bootstrap = new ServerBootstrap();

	public void init() throws InterruptedException {
		bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
				.childHandler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel channel) throws Exception {
						channel.pipeline()
								.addLast(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2))
								.addLast("ServerHandler", new ServerInHandler())
								.addLast(new LengthFieldPrepender(2));

					}

				});
		try {
			ChannelFuture f = bootstrap.bind("127.0.0.1", 8080).addListener(new GenericFutureListener<Future<? super Void>>() {

				@Override
				public void operationComplete(Future<? super Void> future) throws Exception {
					if (future.isSuccess())
						System.out.println("link#");

				}
			});
			f.channel().closeFuture().sync();

		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws InterruptedException {
		new Server().init();
	}
}
