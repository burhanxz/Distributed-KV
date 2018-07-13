package com.xuzhong.rpc.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.xuzhong.rpc.common.IResponseObservable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;

/**
 * represent a netty client introduce invoker binding to a channel .composing of
 * a unique observable, every observer binding to a channel should register to
 * observable
 * 
 * @author bird
 *
 */

public abstract class InvokerCreator {
	/* a observable is unique in rpc client */
	
	private static final IResponseObservable observable = new IResponseObservable();

	private static final NioEventLoopGroup workGroup = new NioEventLoopGroup();
	
	private static final Timer timer = new HashedWheelTimer();
	
	private static final Bootstrap bootstrap = new Bootstrap();

	private InetSocketAddress address;
	
	private List<ChannelHandler> handlers = new ArrayList<>();
//	{
//			new ReconnectHandler(bootstrap, timer, handlers, address),
//			new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),
//			new HeartBeatHandler(),
//			new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2),
//			new ClientResponseHandler(observable),
//			new LengthFieldPrepender(2)		
//	};
	/* connect to server with address */
	protected Invoker createInvoker(InetSocketAddress address) throws InterruptedException {
		
		handlers.add(new ReconnectHandler(bootstrap, timer, handlers, address));
		handlers.add(new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
		handlers.add(new HeartBeatHandler());
		handlers.add(new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2));
		handlers.add(new ClientResponseHandler(observable));
		handlers.add(new LengthFieldPrepender(2));

		bootstrap.group(workGroup).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {

			@Override
			protected void initChannel(SocketChannel ch) throws Exception {

				ch.pipeline().addLast((ChannelHandler[])handlers.toArray());
			}

		});

		ChannelFuture future = bootstrap.connect(address).sync();

		if (future.isSuccess() && future.channel().isActive()) {
			System.out.println("connect success!" + future.channel().remoteAddress());
			Channel channel = future.channel();
			Invoker invoker = new InvokerImpl(channel, address, observable);
			return invoker;
		} else {
			future.cancel(true);
			future.channel().close();
			throw new RuntimeException("connect failed!!");
		}
		
		

	}
}
