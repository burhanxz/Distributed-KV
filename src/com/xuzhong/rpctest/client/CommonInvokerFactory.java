package com.xuzhong.rpctest.client;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * a invoker factory class , and also a netty client introduce variants of
 * channels every of which binding to a specific server
 * 
 * @author bird
 *
 */
public class CommonInvokerFactory extends InvokerCreator implements InvokerFactory {

	private final static CommonInvokerFactory instance = new CommonInvokerFactory();

	private Map<InetSocketAddress, Invoker> invokerMap = new ConcurrentHashMap<>();

	private CommonInvokerFactory() {
	}

	public static CommonInvokerFactory getInstance() {
		return instance;
	}

	@Override
	public synchronized Invoker get(InetSocketAddress address) throws Exception {
		if (!invokerMap.containsKey(address)) {
			invokerMap.putIfAbsent(address, createInvoker(address));
		}

		Invoker invoker = invokerMap.get(address);

		// heart beat package, to be continued...
		/* invoker.startHeartBeat(); */

		return invoker;
	}

	@Override
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
	public synchronized boolean removeAll() {
		try {
			Set<InetSocketAddress> keySet = invokerMap.keySet();
			Iterator<InetSocketAddress> iter = keySet.iterator();
			
			while(iter.hasNext()) {
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
		Invoker invoker = invokerMap.get(address);
		
		return invoker.isConnected();

	}

	@Override
	public Map<InetSocketAddress, Boolean> checkAllConnect() {
		
		Map<InetSocketAddress, Boolean> map = new HashMap<>();
		
		Set<InetSocketAddress> keySet = invokerMap.keySet();
		Iterator<InetSocketAddress> iter = keySet.iterator();
		
		while(iter.hasNext()) {
			map.put(iter.next(), invokerMap.get(iter.next()).isConnected());
		}
		
		return map;

	}


}
