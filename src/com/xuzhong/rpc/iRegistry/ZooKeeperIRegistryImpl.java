package com.xuzhong.rpc.iRegistry;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.xuzhong.rpc.client.CommonInvokerFactory;
import com.xuzhong.rpc.client.Invoker;
import com.xuzhong.rpc.service.NameService;

public class ZooKeeperIRegistryImpl implements IRegistry, Watcher{
	
	private InetSocketAddress zkAddress;
	
//	private Invoker invoker;
	
	public ZooKeeperIRegistryImpl(InetSocketAddress address) throws Exception{
		this.zkAddress = address;
//		this.invoker = CommonInvokerFactory.getInstance().get(address);
	}
	
	

	@SuppressWarnings("unchecked")
	@Override
	public <T> T lookup(Class<T> serviceClazz) {
		
		T returnValue = (T)Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class<?>[] {serviceClazz},
				map.get(serviceClazz.getName()));
		
		return returnValue;
		
	}
	



	private static final Executor pool = Executors.newCachedThreadPool();
	
	public static ZooKeeper zk = null;

	protected CountDownLatch countDownLatch = new CountDownLatch(1);

	private static final int SESSION_TIME = 2000;

	private Map<String, RpcInvokeHandler> map = new HashMap<>();

	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected) {
			switch (event.getType()) {
			case None:
				System.out.println("connected!");
				// 连接完成，释放锁，使得connect方法返回
				countDownLatch.countDown();
				break;
			case NodeChildrenChanged:
				try {
					getServices();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			}
		}

	}

	public void connect() {
		try {
			zk = new ZooKeeper("127.0.0.1:3000", SESSION_TIME, this);
			// 等待连接完成，由连接事件解锁
			countDownLatch.await();
			getServices();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getServices() throws KeeperException, InterruptedException, UnsupportedEncodingException {
		List<String> serviceList = zk.getChildren("/", this);
		System.out.println("--------------------------------");
		
		//有服务被删除了，同时也必定被服务器监视器监测到了，相应的监视器已经关闭而失活
		//这是少数事件，可遍历
		if(serviceList.size() < map.size()) {
			Iterator<String> it = map.keySet().iterator();
			while(it.hasNext()) {
				RpcInvokeHandler serverMonitor = (RpcInvokeHandler) map.get(it.next());
				if(serverMonitor.isActive())
					it.remove();
			}
			return;
		}
		
		//新增服务
		for (String service : serviceList) {
			//忽略zookeeper文件，这是用于选举的节点，不是服务节点
			if (service.equals("zookeeper"))
				continue;
			if (!map.containsKey(service)) {
				RpcInvokeHandler serverMonitor = new RpcInvokeHandler(zk, service);
				map.put(service, serverMonitor);
				pool.execute(serverMonitor);
			}
			System.out.println("@@@@@@@@@@@@@@@" + service);
		}
	}

	
	public static void main(String[] args) {

		try {
			new ZooKeeperIRegistryImpl(new InetSocketAddress("127.0.0.1", 8080)).connect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
		}
	}
	
	public void run() {
		try {
			connect();
			//阻塞，等监视器关闭时则结束run
			synchronized (this) {
				this.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void close() {
		synchronized (this) {
			this.notify();
		}
	}

	/*directly return the field address because InetSocketAddress is a immutable class */
	@Override
	public InetSocketAddress getRegisterAddress() {
		return this.zkAddress;
	}
	

//	@Override
//	public void release() {
//		CommonInvokerFactory.getInstance().remove(address);
//	}

}
