package rpc.client;

import java.net.InetSocketAddress;

import org.springframework.beans.factory.FactoryBean;

import rpc.provide.ComputeService;
import rpc.provide.ComputeServiceImpl;
import rpc.registry.Registry;
import rpc.registry.RegistryFactory;

/**
 * @author bird 服务订阅Bean
 * @param <T>
 */
public class Subscribe<T> implements FactoryBean<T> {
	/* 接口类 */
	private Class<T> interfaceClazz;
	/* 配置类，应当用来初始化iRegistry类对象 */
	private SubscribeConfig config;

	/* 返回代理类对象，由netty连接代理 */
	@Override
	public T getObject() throws Exception {
		// 获取注册对象
		Registry iRegistry = RegistryFactory.getInstance().getZooKeeperRegistry();
		// 寻找可用服务
		T stub = iRegistry.lookup(interfaceClazz);
		return stub;
		// 测试用
		// return (T) new ComputeServiceImpl();
	}

	/* 接口类 */
	@Override
	public Class<?> getObjectType() {
		return interfaceClazz;
	}

	/* 是单例类 */
	@Override
	public boolean isSingleton() {
		return true;
	}

	/* getter and setter */
	public Class<?> getInterfaceClazz() {
		return interfaceClazz;
	}

	public void setInterfaceClazz(Class<T> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public SubscribeConfig getConfig() {
		return config;
	}

	public void setConfig(SubscribeConfig config) {
		this.config = config;
	}
}
