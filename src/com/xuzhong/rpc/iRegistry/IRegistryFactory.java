package com.xuzhong.rpc.iRegistry;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class IRegistryFactory {
	private static final IRegistryFactory instance = new IRegistryFactory();
	
	private IRegistryFactory() {}
	
	public static IRegistryFactory getInstance() {
		return instance;
	}
	
	private Map<InetSocketAddress, IRegistry> iRegistryMap = new ConcurrentHashMap<>();
	 
	public IRegistry getZkRegistry(InetSocketAddress address) throws Exception {
		
		if(!iRegistryMap.containsKey(address)) {
			
			IRegistry iRegistry = new ZooKeeperIRegistryImpl(address);
			
			IRegistry iRegistryInMap = iRegistryMap.putIfAbsent(address, iRegistry);
			
			if(iRegistryInMap != null) {
				iRegistry = iRegistryInMap;
			}
			//让注册机运行
			((ZooKeeperIRegistryImpl)iRegistry).run();
			
			return iRegistry;
			
		}
		
		return iRegistryMap.get(address);
		
	}
}
