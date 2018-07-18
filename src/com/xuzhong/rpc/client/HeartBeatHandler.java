package com.xuzhong.rpc.client;

import com.xuzhong.rpc.facet.Log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
/**
 * @author bird
 * 发送心跳包
 * 基于WRITER_IDLE状态
 */
@Sharable
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
	//通用的心跳包数据
	private final static ByteBuf HEARTBEATPACKAGE = Unpooled.copiedBuffer("heartbeat".getBytes());

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		IdleState state = null;
		if (evt instanceof IdleStateEvent)
			state = ((IdleStateEvent) evt).state();
		//对于写空闲，即长时间未传送数据，将传输心跳包
		if (state == IdleState.WRITER_IDLE) {
			Log.logger.info("send heartbeat package");
			ctx.writeAndFlush(HEARTBEATPACKAGE.copy());
			return;
		}
		
		//当UserEventTriggered事件未处理完时执行
		ctx.fireUserEventTriggered(evt);
	}

}
