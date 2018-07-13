package com.xuzhong.rpc.test.heartbeat.client;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

/**
 * @author bird 
 * 		   0) 这个类可以去观察链路是否断了，如果断了，进行循环的断线重连操作，ConnectionWatchdog，顾名思义，链路检测狗
 *         1）继承了ChannelInboundHandlerAdapter，说明它也是Handler，也对，作为一个检测对象，肯定会放在链路中，否则怎么检测
 * 
 *         2）实现了2个接口，TimeTask，ChannelHandlerHolder
 * 
 *         ①TimeTask，我们就要写run方法，这应该是一个定时任务，这个定时任务做的事情应该是重连的工作
 * 
 *         ②ChannelHandlerHolder的接口，这个接口我们刚才说过是维护的所有的Handlers，因为在重连的时候需要获取Handlers
 * 
 *         3）bootstrap对象，重连的时候依旧需要这个对象
 * 
 *         4）当链路断开的时候会触发channelInactive这个方法，也就说触发重连的导火索是从这边开始的
 */
@Sharable
public abstract class ConnectionWatchdog extends ChannelInboundHandlerAdapter
		implements TimerTask, ChannelHandlerHolder {
	private final Bootstrap bootstrap;
	private final Timer timer;
	private final int port;

	private final String host;

	private volatile boolean reconnect = true;
/*	
	Atomic包名为java.util.concurrent.atomic。这个包提供了一系列原子类。这些类可以保证多线程环境下，
	当某个线程在执行atomic的方法时，不会被其他线程打断，而别的线程就像自旋锁一样，一直等到该方法执行完成，
	才由JVM从等待队列中选择一个线程执行。Atomic类在软件层面上是非阻塞的，它的原子性其实是在硬件层面上借助
	相关的指令来保证的。
	*/
	private AtomicInteger attempts = new AtomicInteger(0);

	public ConnectionWatchdog(Bootstrap bootstrap, Timer timer, int port, String host, boolean reconnect) {
		this.bootstrap = bootstrap;
		this.timer = timer;
		this.port = port;
		this.host = host;
		this.reconnect = reconnect;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {

		System.out.println("当前链路已经激活了，重连尝试次数重新置为0");
		attempts.set(0);;
		ctx.fireChannelActive();
	}

	@Override
	/* 重连 时间间隔的算法 */
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("链接关闭");
		if (reconnect) {
			System.out.println("链接关闭，将进行重连");

			synchronized(attempts) {
				if(attempts.get() < 12) {
					int timeout = attempts.incrementAndGet(); //2 * 2^(attempts)
					timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
				}			
			}
		}
		ctx.fireChannelInactive();
	}

	/* 
	 * 线程同步策略:synchronized bootstrap
	 * @see io.netty.util.TimerTask#run(io.netty.util.Timeout)
	 */
	@Override
	public void run(Timeout timeout) throws Exception {
		ChannelFuture future;

		// bootstrap已经初始化好了，只需要将handler填入就可以了
		//由于此部分代码被定时器执行，当时间间隔极短时，多个线程同时对bootstrap的操作，可能会出错，出现线程安全问题
		synchronized (bootstrap) {
			bootstrap.handler(new ChannelInitializer<Channel>() {
				@Override
				protected void initChannel(Channel ch) throws Exception {
					ch.pipeline().addLast(handlers());
				}
			});
			future = bootstrap.connect(host, port);
		}
		// future对象
		future.addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture f) throws Exception {
				boolean succeed = f.isSuccess();
				// 如果重连失败，则调用ChannelInactive方法，再次出发重连事件，一直尝试12次，如果失败则不再重连
				if (!succeed) {
					System.out.println("重连失败");
					f.channel().pipeline().fireChannelInactive();
				} else {
					System.out.println("重连成功");
				}
			}
		});
		

	}

}
