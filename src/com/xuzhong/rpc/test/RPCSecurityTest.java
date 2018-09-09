package com.xuzhong.rpc.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.CountDownLatch;

import com.xuzhong.rpc.lock.RPCSecurity;
import com.xuzhong.rpc.redis.JedisPoolUtil;

public class RPCSecurityTest {

	public static void main(String[] args) {
		JedisPoolUtil.getPool().getResource().flushDB();
		// 确保多个线程能同时运行
		CountDownLatch latch = new CountDownLatch(1);
		for (int i = 0; i != 10; i++) {
			new Thread() {

				@Override
				public void run() {
					try {
						latch.await();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					Foo fooStub = AOP.wrap(Foo.class, new FooImpl());
					Foo stub = (Foo) RPCSecurity.getDistributedLockStub(Foo.class, fooStub);
					stub.print();
				}
			}.start();
		}
		latch.countDown();
	}
	// 测试结果
	// +++++++++++++++++++
	// Thread-12加锁成功
	// 线程：Thread-12
	// Thread-12解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-5加锁成功
	// 线程：Thread-5
	// Thread-5解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-11加锁成功
	// 线程：Thread-11
	// Thread-11解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-4加锁成功
	// 线程：Thread-4
	// Thread-4解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-3加锁成功
	// 线程：Thread-3
	// Thread-3解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-8加锁成功
	// 线程：Thread-8
	// Thread-8解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-7加锁成功
	// 线程：Thread-7
	// Thread-7解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-9加锁成功
	// 线程：Thread-9
	// Thread-9解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-6加锁成功
	// 线程：Thread-6
	// Thread-6解锁成功
	// +++++++++++++++++++
	// +++++++++++++++++++
	// Thread-10加锁成功
	// 线程：Thread-10
	// Thread-10解锁成功
	// +++++++++++++++++++

}

interface Foo {
	void print();
}

class FooImpl implements Foo {

	@Override
	public void print() {
		System.out.println("线程：" + Thread.currentThread().getName());
	}

}
class AOP {
	@SuppressWarnings("unchecked")
	public static <T, P extends T> T wrap(Class<T> interfaceClazz, P implObject) {
		T ret;
		ret = (T) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
				new Class<?>[] { interfaceClazz }, new InvocationHandler() {

					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						Object o = null;
						System.out.println("wrap!!!!");
						o = method.invoke(implObject, args);
						return o;
					}
				});
		return ret;
	}
}
