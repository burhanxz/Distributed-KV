package rpc.impl;

import org.springframework.beans.factory.FactoryBean;

public class ConsumerFactoryBean<T> implements FactoryBean<T>{
	private final Class<T> clazz;
	private final ConsumerConfig config;
	public ConsumerFactoryBean(Class<T> clazz, ConsumerConfig config) {
		this.clazz = clazz;
		this.config = config;
	}
	@Override
	public T getObject() throws Exception {
		// 通过consumer config 获取代理对象
		return config.getProxy(clazz);
	}

	@Override
	public Class<T> getObjectType() {
		return clazz;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


}
