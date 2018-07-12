package com.xuzhong.rpctest.test.heartbeat.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * @author bird
 * HeartBeatServerHandler就是一个很简单的自定义的Handler
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter{

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
        System.out.println("server channelRead..");  
        System.out.println(ctx.channel().remoteAddress() + "->Server :" + msg.toString());  
	}

	@Override
/*	当上个handler（acceptorIdleStateTrigger）抛出异常时，调用此方法*/
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {	
        cause.printStackTrace();  
        ctx.close();  
	}
}
