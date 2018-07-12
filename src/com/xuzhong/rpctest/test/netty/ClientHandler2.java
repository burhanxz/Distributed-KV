package com.xuzhong.rpctest.test.netty;

import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler2 extends ChannelInboundHandlerAdapter{
	Map<String, byte[]> responses = null;
	public ClientHandler2(Map<String, byte[]> responses) {
		this.responses = responses;
	}
	
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {  
        byte[] bytes = ((String)msg).getBytes();

        System.out.println("handler2::ctx::"+ ctx);
       
        System.out.println("handler2::ctx::"+ ctx.fireChannelRead(msg));
        responses.put(new String("@4378hfs"), bytes);
    }  
	
}
