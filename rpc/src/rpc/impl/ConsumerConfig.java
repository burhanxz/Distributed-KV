package rpc.impl;

import rpc.Cluster;

import rpc.Invoker;
import rpc.ProxyFactory;
import rpc.Registry;
import rpc.URL;

public class ConsumerConfig{
	private Cluster cluster;
	private Registry registry;
	private ProxyFactory proxyFactory;
	public ConsumerConfig() {
		//TODO 初始化consumer config;
	}
	public <T> T getProxy(Class<T> clazz){
		// 建立注册中心url，传入类型信息
		URL registryUrl = null;
		// 创建directory
		NotifiedDirectroy<T> directory = new NotifiedDirectroy<T>();
		// 将directory作为监听器，订阅注册中心消息
		registry.subscribe(registryUrl, directory);
		// 将directory作为invoker集合，通过cluster合并成一个虚拟invoker
		Invoker<T> invoker = cluster.join(directory);
		// 通过invoker获取proxy
		T proxy = proxyFactory.getProxy(invoker);
		return proxy;
	}
}
