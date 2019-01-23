package rpc.impl;

import java.lang.reflect.Proxy;
import java.util.Map;

import com.google.common.base.Preconditions;

import rpc.Invoker;
import rpc.ProxyFactory;
import rpc.URL;

/**
 * 利用jdk内部动态代理实现代理工厂
 * @author bird
 *
 */
public class JdkProxyFactory implements ProxyFactory{
	private static volatile ProxyFactory instance;
	private JdkProxyFactory() {}
	public static ProxyFactory getInstance() {
		// 获取单实例对象
		if(instance == null) {
			synchronized(JdkProxyFactory.class) {
				if(instance == null) {
					instance = new JdkProxyFactory();
				}
			}
		}
		return instance;
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getProxy(Invoker<T> invoker, Map<String, String> options) {
		Preconditions.checkNotNull(invoker);
		// 利用java原生动态代理来获取代理对象
		return (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class<?>[]{invoker.getInterface()}, 
				new RpcInvocationHandler(invoker, options));
	}

	@Override
	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
		// TODO Auto-generated method stub
		return null;
	}

}
