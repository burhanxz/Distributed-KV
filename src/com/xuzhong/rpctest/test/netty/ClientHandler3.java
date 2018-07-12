package com.xuzhong.rpctest.test.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ClientHandler3 extends ChannelInboundHandlerAdapter{
	@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {  
        byte[] bytes = ((String)msg).getBytes();

        System.out.println("handler3 ctx::"+ ctx);
       
       
    }  
}
