package com.xuzhong.rpc.iRegistry;

import java.net.InetSocketAddress;

public interface IRegistry {
	
	public <T> T lookup(Class<T> serviceClazz);
	
	public InetSocketAddress getRegisterAddress();
}
