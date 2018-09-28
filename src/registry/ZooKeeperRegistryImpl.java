package registry;

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

import conf.Config;
import redis.RedisKeyUtil;
import redis.clients.jedis.Jedis;

/**
 * @author bird ZooKeeper实现的IRegistry
 */
public class ZooKeeperRegistryImpl implements Registry{
	/* 用于完成connect操作的锁 */
	protected CountDownLatch countDownLatch = new CountDownLatch(1);
	/**
	 * 服务列表存在redis中
	 * 待完善
	 */
	private Jedis conn = new Jedis("127.0.0.1", 6379);
	// private Invoker invoker;

	/**
	 * @param address
	 *            ZooKeeper连接地址 构造器
	 */
	protected ZooKeeperRegistryImpl() {
	}

	/*
	 * @see zookeeper.IRegistry#lookup(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> T lookup(Class<T> serviceClazz) {
		// 检查redis中是否已经存有此服务的服务列表，如果没有，则通知monitor进行服务列表的初次拉取
		String serviceListKey = RedisKeyUtil.getServiceListKey(serviceClazz);
		if (!conn.exists(serviceListKey)) {
			//获取ZooKeeper监视器
			Monitor monitor = ZooKeeperMonitor.getInstance();
			//初始化相应的服务列表到redis
			monitor.initServiceList(serviceClazz);
		}
		// 核心实现
		// 通过动态代理，获取代理对象
		// 通过map获取RPCInvokeHandler对象
		T returnValue = (T) Proxy.newProxyInstance(serviceClazz.getClassLoader(), new Class<?>[] { serviceClazz },
				new RPCInvocationHandler(serviceClazz));
		
		return returnValue;

	}

	@Override
	public <T> InetSocketAddress getServerAddress(Class<T> serviceClazz) {
		// TODO Auto-generated method stub
		return null;
	}

}