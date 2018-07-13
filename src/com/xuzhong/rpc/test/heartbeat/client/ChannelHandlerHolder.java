package com.xuzhong.rpc.test.heartbeat.client;

import io.netty.channel.ChannelHandler;

public interface ChannelHandlerHolder {
	ChannelHandler[] handlers();
}
