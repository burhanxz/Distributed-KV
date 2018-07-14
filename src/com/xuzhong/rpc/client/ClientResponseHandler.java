package com.xuzhong.rpc.client;

import java.net.InetSocketAddress;

import com.xuzhong.rpc.common.IResponseObservable;
import com.xuzhong.rpc.common.data.IResponse;
import com.xuzhong.rpc.facet.Log;
import com.xuzhong.rpc.util.ProtostuffUtil;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * a handler designed to handle response from remote server
 * 
 * @author bird
 *
 */
@Sharable
public class ClientResponseHandler extends ChannelInboundHandlerAdapter {
	private IResponseObservable observable;

	public ClientResponseHandler(IResponseObservable observable) {
		this.observable = observable;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		
		if (msg instanceof ByteBuf) {
			ByteBuf buff = (ByteBuf) msg;

			byte[] bytes = new byte[buff.readableBytes()];

			buff.readBytes(bytes);

			IResponse iResponse = ProtostuffUtil.deserializer(bytes, IResponse.class);

			Log.logger.info(iResponse);

			observable.pushResponse(iResponse, (InetSocketAddress) ctx.channel().remoteAddress());
		}

		else

			ctx.fireChannelRead(msg);
	}
}
