package rpc.impl;

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
		if(instance == null) {
			synchronized(JdkProxyFactory.class) {
				if(instance == null) {
					instance = new JdkProxyFactory();
				}
			}
		}
		return instance;
	}
	@Override
	public <T> T getProxy(Invoker<T> invoker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url) {
		// TODO Auto-generated method stub
		return null;
	}

}
