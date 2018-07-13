package com.xuzhong.rpc.conf;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xuzhong.rpc.client.ClientResponseHandler;
import com.xuzhong.rpc.client.HeartBeatHandler;
import com.xuzhong.rpc.client.ReconnectHandler;
import com.xuzhong.rpc.common.IResponseObservable;

import io.netty.bootstrap.Bootstrap;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

/**
 * @author bird
 * 配置client
 */
@Deprecated
@Configuration
public class ClientConfig {
	
	@Bean
	public Bootstrap getBootstrap() {		
		return new Bootstrap();
	}
	
	@Bean
	public IResponseObservable getIResponseObservable() {
		return new IResponseObservable();
	}
	
	@Bean
	public Timer getTimer() {
		return new HashedWheelTimer();
	}
	
//	@Bean
//	public ReconnectTask getReconnectTask() {
//		return new ReconnectTask();
//	}
	
	@Bean
	public HeartBeatHandler getHeartBeatHandler() {
		return new HeartBeatHandler();
	}
	
	@Bean
	public ClientResponseHandler getClientResponseHandler() {
		return new ClientResponseHandler(this.getIResponseObservable());
	}
	
//	@Bean
//	public ReconnectHandler getReconnectHandler() {
//		return new ReconnectHandler();
//	}
	
	@Bean
	public IdleStateHandler getIdleStateHandler() {
		return new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS);
	}
	
	@Bean
	public LengthFieldBasedFrameDecoder getLengthFieldBasedFrameDecoder() {
		return new LengthFieldBasedFrameDecoder(1024, 0, 2, 0, 2);
	}
	
	@Bean
	public LengthFieldPrepender getLengthFieldPrepender() {
		return new LengthFieldPrepender(2);
	}
}
