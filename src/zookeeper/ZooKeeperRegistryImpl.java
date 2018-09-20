package zookeeper;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooKeeper;

import redis.clients.jedis.Jedis;

/**
 * @author bird ZooKeeper实现的IRegistry
 */
public class ZooKeeperRegistryImpl implements Registry, Watcher {
	/* ZooKeeper的地址 */
	private InetSocketAddress zkAddress;
	/* ???? */
	private static final Executor pool = Executors.newCachedThreadPool();
	/* ZooKeeper连接类对象 */
	public static ZooKeeper zk = null;
	/* 用于完成connect操作的锁 */
	protected CountDownLatch countDownLatch = new CountDownLatch(1);
	/* ZooKeeper参数，会话时间限制 */
	private static final int SESSION_TIME = 2000;
	/* 用于存放ServiceInfo对象->RPCInvokeHandler对象的Map */
	private Map<String, RpcInvokeHandler> Service2RPCInvokeHandler = new HashMap<>();
	/**
	 * 服务列表存在redis中
	 */
	private Jedis conn = new Jedis("127.0.0.1", 6379);
	// private Invoker invoker;

	/**
	 * @param address
	 *            ZooKeeper连接地址 构造器
	 */
	public ZooKeeperRegistryImpl(InetSocketAddress address) {
		this.zkAddress = address;
		// this.invoker = CommonInvokerFactory.getInstance().get(address);
	}

	/*
	 * @see zookeeper.IRegistry#lookup(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T lookup(Class<T> serviceClazz) {
		// 核心实现
		// 通过动态代理，获取代理对象
		// 通过map获取RPCInvokeHandler对象
		// 检查redis中是否已经存有此服务的服务列表，如果没有，则通知monitor进行服务列表的初次拉取
		if (!conn.exists("app1:" + serviceClazz.getName())) {
			
		}
		T returnValue = (T) Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class<?>[] { serviceClazz },
				new RPCInvocationHandler(serviceClazz));

		return null;

	}

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

	public void getServices() throws KeeperException, InterruptedException, UnsupportedEncodingException, Exception {
		List<String> serviceList = zk.getChildren("/", this);
		System.out.println("--------------------------------");

		// 有服务被删除了，同时也必定被服务器监视器监测到了，相应的监视器已经关闭而失活
		// 这是少数事件，可遍历
		if (serviceList.size() < Service2RPCInvokeHandler.size()) {
			Iterator<String> it = Service2RPCInvokeHandler.keySet().iterator();
			while (it.hasNext()) {
				RpcInvokeHandler serverMonitor = (RpcInvokeHandler) Service2RPCInvokeHandler.get(it.next());
				if (serverMonitor.isActive())
					it.remove();
			}
			return;
		}

		// 新增服务
		for (String service : serviceList) {
			// 忽略zookeeper文件，这是用于选举的节点，不是服务节点
			if (service.equals("zookeeper"))
				continue;
			// 将服务名称转化为服务类
			Class<?> serviceInterface = Class.forName("service." + service);
			// >>>>>>>>>>>>>>>>>>>>
			if (!Service2RPCInvokeHandler.containsKey(service)) {
				RpcInvokeHandler serverMonitor = new RpcInvokeHandler(zk, serviceInterface);
				Service2RPCInvokeHandler.put(service, serverMonitor);
				pool.execute(serverMonitor);
			}
			System.out.println("@@@@@@@@@@@@@@@" + service);
		}
	}

	public static void main(String[] args) {

		try {
			new ZooKeeperRegistryImpl(new InetSocketAddress("127.0.0.1", 8080)).connect();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
		}
	}

	public void run() {
		try {
			connect();
			// 阻塞，等监视器关闭时则结束run
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

	/*
	 * directly return the field address because InetSocketAddress is a immutable
	 * class
	 */
	@Override
	public InetSocketAddress getRegisterAddress() {
		return this.zkAddress;
	}

	// @Override
	// public void release() {
	// CommonInvokerFactory.getInstance().remove(address);
	// }

}
