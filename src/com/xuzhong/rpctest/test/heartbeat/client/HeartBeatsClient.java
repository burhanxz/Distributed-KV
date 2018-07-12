package com.xuzhong.rpctest.test.heartbeat.client;

import java.util.concurrent.TimeUnit;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;

/**
 * @author bird 1）创建了ConnectionWatchdog对象，自然要实现handlers方法
 * 
 *         2）初始化好bootstrap对象
 * 
 *         3）4秒内没有写操作，进行心跳触发，也就是IdleStateHandler这个方法
 */
public class HeartBeatsClient {
	protected final HashedWheelTimer timer = new HashedWheelTimer();

	private Bootstrap boot;

	private final ConnectorIdleStateTrigger idleStateTrigger = new ConnectorIdleStateTrigger();

	public void connect(int port, String host) throws Exception {
		EventLoopGroup group = new NioEventLoopGroup();
        boot = new Bootstrap();  
        boot.group(group).channel(NioSocketChannel.class)
                         .handler(new LoggingHandler(LogLevel.INFO));  
    	final ConnectionWatchdog watchdog = new ConnectionWatchdog(boot, timer, port, host, true) {
    		public ChannelHandler[] handlers() {
                return new ChannelHandler[] {
                        this,  //in 
                        /*
                         * Triggers an IdleStateEvent when a Channel 
                         * has not performed read, write, or both operation for a while.
                         * IdleStateHandler(long readerIdleTime, long writerIdleTime, 
                         * long allIdleTime, java.util.concurrent.TimeUnit unit) 
                         */
                        new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS),  //in
                        idleStateTrigger,  //in
                        new StringDecoder(),  //in
                        new StringEncoder(),  //out
                        new HeartBeatClientHandler()  //in
                };
    		}
    	};
    	
    	ChannelFuture future;
		try {
			synchronized (boot) {
				boot.handler(new ChannelInitializer<Channel>() {
					// 初始化channel
					@Override
					protected void initChannel(Channel ch) throws Exception {
						ch.pipeline().addLast(watchdog.handlers());
					}
				});

				future = boot.connect(host, port);
			}
			future.sync();
		} catch (Throwable t) {
			throw new Exception("connects to  fails", t);
		}
	}
}
