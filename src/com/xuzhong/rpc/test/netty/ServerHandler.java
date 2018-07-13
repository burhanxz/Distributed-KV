package com.xuzhong.rpc.test.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;  
/** 
 *  
  * @ClassName: ServerHandler 
  * @Description: TODO 
  * @author xiefg 
  * @date 2016年8月4日 下午5:34:19 
  * 
 */  
public class ServerHandler extends ChannelInboundHandlerAdapter {  
      
      
    @Override  
    public void channelRead(ChannelHandlerContext ctx, Object msg)  
            throws Exception {  
        
        System.out.println("server receive message :"+ msg);  
        ctx.channel().writeAndFlush("yes server already accept your message" + msg);  
        ctx.close();  
    }  
    @Override  
    public void channelActive(ChannelHandlerContext ctx) throws Exception {  
        // TODO Auto-generated method stub  
        System.out.println("【channelActive】。。。");  
    }  
      @Override  
   public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {  
        System.out.println("【exception is general】");  
    }  
}  
