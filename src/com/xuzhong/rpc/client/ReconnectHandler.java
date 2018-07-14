package com.xuzhong.rpc.client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.xuzhong.rpc.facet.Log;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
@Sharable
public class ReconnectHandler extends ChannelInboundHandlerAdapter{
	private static final int MAXRECONNECTTIMES = 5;
	
	private Bootstrap boot;
	
	private AtomicInteger times = new AtomicInteger(0);
	
	private Timer timer;
	
	private List<ChannelHandler> handlers;
	
	private InetSocketAddress address;
	
	public ReconnectHandler(Bootstrap boot, Timer timer, List<ChannelHandler> handlers, InetSocketAddress address) {
		this.boot = boot;
		this.timer = timer;
		this.handlers = handlers;
		this.address = address;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		Log.logger.info("ReconnectHandler: channel active");
		times.set(0);
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		Log.logger.info("ReconnectHandler: channel inactive");
		synchronized(times) {
			if(times.get() < MAXRECONNECTTIMES) {
				int delay = 2 << times.incrementAndGet() ;
				
				timer.newTimeout(new ReconnectTask(), delay, TimeUnit.MILLISECONDS);
			}
		}

		
	}
	
	private class ReconnectTask implements TimerTask{

		@Override
		public void run(Timeout timeout) throws Exception {
			synchronized(boot) {
				boot.handler(new ChannelInitializer<Channel>() {

					@Override
					protected void initChannel(Channel ch) throws Exception {
						
						ch.pipeline().addLast((ChannelHandler[])handlers.toArray());

					}
					
				});
				boot.connect(address).addListener(new ChannelFutureListener() {

					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (future.isSuccess())
							future.channel().pipeline().fireChannelActive();
						else
							future.channel().pipeline().fireChannelInactive();

					}
				});
			}

			
		}
		
	}

	
}
