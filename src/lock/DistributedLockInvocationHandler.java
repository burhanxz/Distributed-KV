package lock;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import conf.Config;

/**
 * @author bird
 * 是加锁动态代理的核心实现类
 */
public class DistributedLockInvocationHandler implements InvocationHandler{
	/*实现类的对象*/
	Object implObject;
	/*初始化*/
	public DistributedLockInvocationHandler(Object impl) {
		this.implObject = impl;
	}
	
	/* 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object, java.lang.reflect.Method, java.lang.Object[])
	 */
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object result = null;
		//根据配置来选择分布式锁的实现方式
		DistributedLock lock = (DistributedLock) Class.forName(Config.getDistributedLockImpl()).newInstance();
		//锁序列化号码
		String lockSerialNumber = null;
		try {
			//加锁，以类名+方法名为锁名
			lockSerialNumber = lock.lock(implObject.getClass().getName() + "." + method.getName());
			//实际方法调用过程
			result = method.invoke(implObject, args);
			
		} finally {
			//解锁
			lock.unLock(method.getName(), lockSerialNumber);
		}
		return result;
	}

}
