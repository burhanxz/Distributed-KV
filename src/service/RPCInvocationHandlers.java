package service;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RPCInvocationHandlers {
	/**
	 * 
	 */
	private static Map<Class<?>, WeakReference<RPCInvocationHandler>> invocationHandlers = new ConcurrentHashMap<>();
	/**
	 * 
	 */
	private static Lock lock = new ReentrantLock();
	private static ReferenceQueue<RPCInvocationHandler> queue = new ReferenceQueue<>();
	/**
	 * @param serviceInterface
	 * @return
	 */
//	public static RPCInvocationHandler getInvocationHandlers(Class<?> serviceInterface) {
//		RPCInvocationHandler ret = null;
//
//		
//		
//	}
//	private void createInvocationHandler() {
//		
//	}
}
