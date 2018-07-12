package com.xuzhong.rpctest.client;

import java.lang.reflect.Method;

import com.xuzhong.rpctest.common.data.IRequest;
import com.xuzhong.rpctest.common.data.IResponse;

/**
 * a invoker is used to invoke remote service (or method), binding to a specific server
 * @author bird
 *
 */
public interface Invoker {
	
	public Object invoke(Class<?> interfaceClazz, Method method, Object[] parameters);
	
	public void startHeartBeat() throws Exception;
	
	public String getDomain();
	
	public boolean isConnected();
	
	public boolean terminateChannel();
}
