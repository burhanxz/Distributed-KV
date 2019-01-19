package rpc.impl;

import rpc.Registry;
import rpc.RegistryFactory;
import rpc.URL;

/**
 * 基于Zookeeper实现的注册中心工厂
 * @author bird
 *
 */
public class ZookeeperRegistryFactory implements RegistryFactory{
	private static volatile RegistryFactory instance;

	private ZookeeperRegistryFactory() {
		// TODO
	}
	
	public static RegistryFactory getInstance() {
		if(instance == null) {
			synchronized (ZookeeperRegistryFactory.class) {
				if(instance == null) {
					instance = new ZookeeperRegistryFactory();
				}
			}
		}
		return instance;
	}
	
	@Override
	public Registry getRegistry(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

}
