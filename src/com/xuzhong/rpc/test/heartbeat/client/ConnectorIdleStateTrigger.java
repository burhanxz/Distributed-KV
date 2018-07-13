package com.xuzhong.rpc.test.heartbeat.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
@Sharable
public class ConnectorIdleStateTrigger extends ChannelInboundHandlerAdapter{
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",  
            CharsetUtil.UTF_8));

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		
		if (evt instanceof IdleStateEvent) {
			IdleState state = ((IdleStateEvent) evt).state();
			
/*			READER_IDLE:
			No data was received for a while.
			WRITER_IDLE:
			No data was sent for a while.*/
/*			长时间没有send数据给服务器了，就发送一个心跳包*/
            if (state == IdleState.WRITER_IDLE) {  
                // write heart beat to server  
                ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());  
            } 
		} else {  
            super.userEventTriggered(ctx, evt);  
        }  
	} 
    
    
}
