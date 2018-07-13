package com.xuzhong.rpc.test.netty;

import io.netty.channel.ChannelHandlerContext;  
import io.netty.channel.ChannelInboundHandlerAdapter;  
/** 
 *  
  * @ClassName: Client 
  * @Description: TODO 
  * @author xiefg 
  * @date 2016年8月4日 下午6:18:08 
  * 
 */  
public class ClientHandler1 extends ChannelInboundHandlerAdapter {  
    @Override  
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {  
        System.out.println("【client接收到服务器返回的消息】:" + msg);  
        System.out.println("handler1::ctx::"+ ctx);
        System.out.println("handler1::ctx::"+ ctx.fireChannelRead(msg));
        
    }  
  
    @Override  
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {  
        System.out.println("【client exception is general】");  
    }  
}  