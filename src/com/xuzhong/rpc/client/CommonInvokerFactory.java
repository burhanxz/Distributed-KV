package com.xuzhong.rpc.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.runner.notification.RunListener.ThreadSafe;

/**
 * a invoker factory class , and also a netty client pool introduce variants of
 * channels every of which binding to a specific server
 * 
 * 一个invoker工厂类，也是一个客户端连接池，它分发和不同服务器绑定的channel
 * 
 * @author bird
 *
 */
@ThreadSafe
public class CommonInvokerFactory extends InvokerCreator implements InvokerFactory {

	private final static CommonInvokerFactory instance = new CommonInvokerFactory();

	// store invokers
	private Map<InetSocketAddress, Invoker> invokerMap = new ConcurrentHashMap<>();
	//用于互斥地建立连接的锁
	private static final Lock lock = new ReentrantLock();
	
	private CommonInvokerFactory() {
	}

	public static CommonInvokerFactory getInstance() {
		return instance;
	}
	
	@Override
	//线程安全地获取invoker
	public Invoker get(InetSocketAddress address) throws Exception {
		// if (!invokerMap.containsKey(address)) {
		// invokerMap.putIfAbsent(address, createInvoker(address));
		// }
		/* 针对耗时的创建对象操作，宜用这种方式操作，兼顾效率和线程安全性 */
		while (!invokerMap.containsKey(address)) {
			if (lock.tryLock()) {
				try {
					invokerMap.putIfAbsent(address, createInvoker(address));
				} finally {
					lock.unlock();
				}
			}
		}

		Invoker invoker = invokerMap.get(address);

		return invoker;
	}

	@Override
	//只读操作，列出所有invoker
	public List<Invoker> listAll() {
		List<Invoker> resultList = new ArrayList<Invoker>();

		Set<InetSocketAddress> keySet = invokerMap.keySet();

		Iterator<InetSocketAddress> iter = keySet.iterator();

		while (iter.hasNext()) {
			InetSocketAddress address = iter.next();

			resultList.add(invokerMap.get(address));
		}

		return resultList;
	}

	@Override
	public boolean remove(InetSocketAddress address) {

		Invoker invoker = null;

		try {
			synchronized (invokerMap) {
				if (!invokerMap.containsKey(address)) {
					throw new RuntimeException("invoker doesn't exit");
				}

				invoker = invokerMap.get(address);
			}

			invoker.terminateChannel();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		return true;

	}

	@Override
	public boolean removeAll() {
		try {
			Set<InetSocketAddress> keySet = invokerMap.keySet();
			Iterator<InetSocketAddress> iter = keySet.iterator();

			while (iter.hasNext()) {
				Invoker invoker = invokerMap.get(iter.next());
				invoker.terminateChannel();
				iter.remove();
			}
		} catch (Exception e) {
			e.printStackTrace();

			return false;
		}

		return true;
	}

	@Override
	public boolean checkConnect(InetSocketAddress address) {
		if (!invokerMap.containsKey(address)) {
			return false;
		}
		Invoker invoker = invokerMap.get(address);

		return invoker.isConnected();

	}

	@Override
	public Map<InetSocketAddress, Boolean> checkAllConnect() {

		Map<InetSocketAddress, Boolean> map = new HashMap<>();

		Set<InetSocketAddress> keySet = invokerMap.keySet();
		Iterator<InetSocketAddress> iter = keySet.iterator();

		while (iter.hasNext()) {
			map.put(iter.next(), invokerMap.get(iter.next()).isConnected());
		}

		return map;

	}

}
