package com.xuzhong.rpctest.test.heartbeat.client;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {
	ChannelHandler[] handlers();
}
