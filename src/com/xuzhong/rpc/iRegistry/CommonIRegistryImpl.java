package com.xuzhong.rpc.iRegistry;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

import com.xuzhong.rpc.client.CommonInvokerFactory;
import com.xuzhong.rpc.client.Invoker;
import com.xuzhong.rpc.service.NameService;

public class CommonIRegistryImpl implements IRegistry{
	
	private InetSocketAddress address;
	
	private Invoker invoker;
	
	protected CommonIRegistryImpl(InetSocketAddress address) throws Exception{
		this.address = address;
		this.invoker = CommonInvokerFactory.getInstance().get(address);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getService(Class<T> serviceClazz) {
		
		T returnValue = (T)Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class<?>[] {serviceClazz},
				new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						
						Object result = invoker.invoke(serviceClazz, method, args);
						
						if(result instanceof Throwable) 
							throw (Throwable)result;
						
						return result;
					}
			
		});
		
		return returnValue;
		
	}
	
	/*directly return the field address because InetSocketAddress is a immutable class */
	@Override
	public InetSocketAddress getRemoteAddress() {
		return this.address;
	}
	

	@Override
	public void release() {
		CommonInvokerFactory.getInstance().remove(address);
	}





}
