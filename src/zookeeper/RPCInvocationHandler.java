package zookeeper;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import client.CommonInvokerFactory;
import client.Invoker;
import redis.RedisKeyUtil;

/**
 * @author bird
 * 动态代理核心实现
 */
public class RPCInvocationHandler implements InvocationHandler{
	/**
	 * redis中存放服务列表信息的键
	 */
	private String serviceListKey;
	/**
	 * 服务类接口
	 */
	private Class<?> serviceInterface;
	public RPCInvocationHandler(Class<?> serviceInterface) {
		serviceListKey = RedisKeyUtil.getServiceListKey(serviceInterface);
		this.serviceInterface = serviceInterface;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		/*此处获取Redis中服务列表的最优值，利用负载均衡*/
		
		//暂时设置
		String host = "";
		int port = 0;
		
		/* 服务器的注册与发现，和服务器的连接，是完全解耦的 */
		// 连接服务器，获取invoker
		Invoker invoker = CommonInvokerFactory.getInstance().get(new InetSocketAddress(host, port));
		Object result = invoker.invoke(serviceInterface, method, args);
		//如果获取的是Throwable类型，则抛出
		if (result instanceof Throwable)
			throw (Throwable) result;
		
		return result;

	}

}
