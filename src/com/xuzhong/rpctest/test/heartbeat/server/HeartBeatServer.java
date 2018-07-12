package com.xuzhong.rpctest.test.heartbeat.server;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

public class HeartBeatServer {
	private final AcceptorIdleStateTrigger idleStateTrigger = new AcceptorIdleStateTrigger();

	private int port;

	public HeartBeatServer(int port) {
		this.port = port;
	}

	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap sbs = new ServerBootstrap().group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

						@Override
						protected void initChannel(SocketChannel ch) throws Exception {
							ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));//in
							ch.pipeline().addLast(idleStateTrigger);//in
							ch.pipeline().addLast("decoder", new StringDecoder());//in
							ch.pipeline().addLast("encoder", new StringEncoder());//out
							ch.pipeline().addLast(new HeartBeatServerHandler());//in
						}
					}).option(ChannelOption.SO_BACKLOG, 128) // 这个设置？
					.childOption(ChannelOption.SO_KEEPALIVE, true);
			// 绑定端口，开始接收进来的连接
			ChannelFuture future;

			future = sbs.bind(port).sync();

			System.out.println("Server start listen at " + port);
			future.channel().closeFuture().sync();

		} catch (InterruptedException e) {
            bossGroup.shutdownGracefully();  
            workerGroup.shutdownGracefully();  
		}
	}
	
    public static void main(String[] args) throws Exception {  
        int port;  
        if (args.length > 0) {  
            port = Integer.parseInt(args[0]);  
        } else {  
            port = 8080;  
        }  
        new HeartBeatServer(port).start();  
    }
}
