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
	 
	public IRegistry getIRegistry(InetSocketAddress address) throws Exception {
		
		if(!iRegistryMap.containsKey(address)) {
			
			IRegistry iRegistry = new CommonIRegistryImpl(address);
			
			IRegistry iRegistryInMap = iRegistryMap.putIfAbsent(address, iRegistry);
			
			if(iRegistryInMap != null) {
				iRegistry = iRegistryInMap;
			}
			
			return iRegistry;
			
		}
		
		return iRegistryMap.get(address);
		
	}
}
