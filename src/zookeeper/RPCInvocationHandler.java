package zookeeper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author bird
 * 待完善
 */
public class RPCInvocationHandler implements InvocationHandler{
//	private Class<?> serviceInterface;
	
	public RPCInvocationHandler(Class<?> serviceInterface) {
//		this.serviceInterface = serviceInterface;
	}
	
	@Override
	public Object invoke(Object arg0, Method arg1, Object[] arg2) throws Throwable {
		
		return null;
	}

}
