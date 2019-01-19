package rpc.impl;

import java.io.IOException;
import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import rpc.Registry;
import rpc.RegistryFactory;
import rpc.URL;

/**
 * 基于Zookeeper实现的注册中心工厂
 * @author bird
 *
 */
public class ZookeeperRegistryFactory implements RegistryFactory{
	/**
	 * 单一实例
	 */
	private static volatile RegistryFactory instance;
	/**
	 * 注册中心缓存
	 */
	private Map<URL, Registry> registries;
	private ZookeeperRegistryFactory() {
		// 初始化缓存，缓存是线程安全的
		registries = new ConcurrentHashMap<>();
	}
	
	public static RegistryFactory getInstance() {
		//如果实例不存在，则初始化实例
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
	public Registry getRegistry(URL url) throws IOException {
		Registry registry = null;
		// 如果registry缓存中不存在
		if((registry = registries.get(url)) == null) {
			synchronized(this) {
				if((registry = registries.get(url)) == null) {
					// 新建registry并放入缓存
					registry = new ZookeeperRegistry(url);
					registries.put(url, registry);
				}
			}
		}
		else {
			// 如果获取到的registry不可用，则从缓存中移除
			if(!registry.isAvailable()) {
				registries.remove(url);
				registry = null;
			}
		}
		return registry;
	}

}
