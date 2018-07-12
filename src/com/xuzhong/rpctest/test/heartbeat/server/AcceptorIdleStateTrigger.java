package com.xuzhong.rpctest.test.heartbeat.server;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * @author bird 
 * 单独写一个AcceptorIdleStateTrigger，其实也是继承
 * ChannelInboundHandlerAdapter，重写userEventTriggered方法，
 *  因为客户端是write，
 * 那么服务端自然是read，设置的状态就是IdleState.READER_IDLE
 */
@Sharable
public class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			if (state == IdleState.READER_IDLE) {
				throw new Exception("idle exception");
			}
		} else {
			super.userEventTriggered(ctx, evt);
		}
	}

}
