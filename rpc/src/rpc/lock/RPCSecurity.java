package rpc.lock;

import java.lang.reflect.Proxy;
/*RPCSecurity动态代理应当使用在rpc远程调用动态代理之前*/
/**
 * @author bird
 * 为类和对象的方法提供分布式锁，实现不同节点之间的线程安全
 */
public class RPCSecurity {
	/**
	 * @param interfaceClazz 接口的Class类
	 * @param implObject 被动态代理的实现类对象
	 * @return 返回已经被动态代理处理的stub
	 */
	@SuppressWarnings("unchecked")
	public static <T, P extends T> Object getDistributedLockStub(Class<T> interfaceClazz, P implObject) {
		T ret = null;
		//利用DistributedLockInvocationHandler将类和对象的方法自动加上分布式锁
		ret = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{interfaceClazz}, 
				new DistributedLockInvocationHandler(implObject));
		
		return ret;
	}

}
