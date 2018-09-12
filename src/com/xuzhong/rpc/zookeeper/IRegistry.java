package com.xuzhong.rpc.zookeeper;

import java.net.InetSocketAddress;

public interface IRegistry {
	
	public <T> T lookup(Class<T> serviceClazz);
	
	public InetSocketAddress getRegisterAddress();
}
