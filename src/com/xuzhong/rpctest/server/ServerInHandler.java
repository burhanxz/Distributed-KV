package com.xuzhong.rpctest.server;

import com.xuzhong.rpctest.common.data.IRequest;
import com.xuzhong.rpctest.common.data.IResponse;
import com.xuzhong.rpctest.service.ComputeService;
import com.xuzhong.rpctest.service.ComputeServiceImpl;
import com.xuzhong.rpctest.service.NameService;
import com.xuzhong.rpctest.service.NameServiceImpl;
import com.xuzhong.rpctest.util.ProtostuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
@Sharable
public class ServerInHandler extends ChannelInboundHandlerAdapter{

	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	
		ByteBuf buff = (ByteBuf) msg;
		byte[] bytes = new byte[buff.readableBytes()];
		buff.readBytes(bytes);
		
		
		IRequest ir = ProtostuffUtil.deserializer(bytes, IRequest.class);
		
		System.out.println(ctx.channel().remoteAddress().toString() + ":" + ir);
		

		
		NameService service1 = new NameServiceImpl();
		ComputeService service2 = new ComputeServiceImpl();
		
		
		Object o = null;
		if(ir.getInterfaceClazz() == service1.getClass().getInterfaces()[0])
			o = ir.getMethod().invoke(service1);
		else {
			o = ir.getMethod().invoke(service2, ir.getParameters());
		}
		
		
		IResponse iResponse = new IResponse();
		
		iResponse.setReturnValue(o);
		
		iResponse.setId(ir.getId());
		
		byte[] resBytes = ProtostuffUtil.serializer(iResponse);
		
		System.out.println(iResponse);
		
		ctx.channel().writeAndFlush(Unpooled.copiedBuffer(resBytes));
		
		
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		System.out.println("channel is active !");
		System.out.println(ctx.channel().remoteAddress());
	}


	
}
