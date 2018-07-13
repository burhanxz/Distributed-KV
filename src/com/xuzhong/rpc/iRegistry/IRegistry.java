package com.xuzhong.rpc.iRegistry;

import java.net.InetSocketAddress;

public interface IRegistry {
	
	public <T> T getService(Class<T> serviceClazz);
	
	public InetSocketAddress getRemoteAddress();
	
	public void release();
}
