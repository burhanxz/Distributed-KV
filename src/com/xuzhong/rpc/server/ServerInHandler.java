package com.xuzhong.rpc.server;

import com.xuzhong.rpc.common.data.IRequest;
import com.xuzhong.rpc.common.data.IResponse;
import com.xuzhong.rpc.service.ComputeService;
import com.xuzhong.rpc.service.ComputeServiceImpl;
import com.xuzhong.rpc.service.NameService;
import com.xuzhong.rpc.service.NameServiceImpl;
import com.xuzhong.rpc.util.ProtostuffUtil;

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
