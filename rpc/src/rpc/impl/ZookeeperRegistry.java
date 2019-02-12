package rpc.impl;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import rpc.NotifyListener;
import rpc.Registry;
import rpc.URL;
import rpc.model.ConsumerConstants;
import rpc.model.ProviderConstants;

/**
 * ZK节点格式：
 *	 						ZK
 * 							|
 * 					appKey1   appKey2...
 * 					   |        
 * 			service1      service2...
 * 				|				  
 * consumers  		providers
 * 		|      			|
 * ip1    ip2...    ip1    ip2...
 * 
 * @author bird
 *
 */
public class ZookeeperRegistry implements Registry{
	/**
	 * zk连接超时时间
	 */
	private static final int ZK_CONNECT_TIMEOUT = 1000;
	/**
	 * zookeeper客户端
	 */
	private ZooKeeper zk;
	/**
	 * zk注册中心全局锁
	 */
	private final Lock mutex = new ReentrantLock();
	/**
	 * 连接状态锁
	 */
	private final Condition connCondition = mutex.newCondition(); 
	/**
	 * 注册中心url
	 */
	private final URL registryUrl;
	/**
	 * 标识zookeeper连接中心是否可用
	 */
	private volatile boolean isAvailable; 
	public ZookeeperRegistry(URL registryUrl) throws IOException {
		Preconditions.checkNotNull(registryUrl);
		this.registryUrl = registryUrl;
		// 初始化zk注册中心
		init();
	}
	/**
	 * 初始化注册中心
	 * @throws IOException zookeeper连接异常
	 */
	private void init() throws IOException {
		// 获取zookeeper连接IP
		String connectString = registryUrl.connectString();
		Preconditions.checkNotNull(connectString);
		mutex.lock();
		try {
			// 新建zookeeper连接
			zk = new ZooKeeper(connectString, ZK_CONNECT_TIMEOUT, new ConnectionWatcher());
			// 等待连接完成,超时时间设置为2倍zk连接超时时间
			connCondition.await(ZK_CONNECT_TIMEOUT << 1, TimeUnit.MILLISECONDS);
			// 设置初始状态
			this.isAvailable = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}finally {
			mutex.unlock();
		}
	}
	
	private class ConnectionWatcher implements Watcher{
		/* 
		 * 当连接成功时，供回调
		 */
		@Override
		public void process(WatchedEvent event) {
			// 传来NONE类型事件，一般是连接事件等
			if(event.getType() == EventType.None) {
				// 事件状态为：连接中
				if(event.getState() == KeeperState.SyncConnected) {
					// 唤醒等待连接成功的线程
					mutex.lock();
					try {
						// 唤醒等待连接成功的线程
						connCondition.signalAll();
					} finally {
						mutex.unlock();
					}
				}
			}
			
		}
		
	}
	
	private class ChildrenWatcher implements Watcher{
		/**
		 * 监视器所监视的zk路径
		 */
		private String path;
		/**
		 * url列表的监听器
		 */
		private NotifyListener listener;
		public ChildrenWatcher(String path, NotifyListener listener){
			this.path = path;
			this.listener = listener;
		}
		@Override
		public void process(WatchedEvent event) {
			// 如果和zk服务器保持连接中
			if(event.getState() == KeeperState.SyncConnected) {
				switch(event.getType()) {
				// providers子节点发生改变，更新服务列表
				case NodeChildrenChanged:{
					try {
						// 获取发生变动的列表，重置监听器
						List<String> paths = zk.getChildren(path, this);
						// 通知listener
						doNotify(listener, paths);
					} catch (KeeperException e) {		
						e.printStackTrace();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				default :{
					//TODO
				}
				}
			}

		}
		
	}
	
	@Override
	public URL getUrl() {
		return registryUrl;
	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void register(URL url) throws Exception {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(url.getPath());
		// 获取url对应的zk路径
		String providerPath = getProviderPath(url);
		// 在zk服务器上建立相应路径
		zk.create(providerPath, "".getBytes(), null, CreateMode.EPHEMERAL);
	}

	@Override
	public void unregister(URL url) throws Exception {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(url.getPath());
		// 获取url对应的zk路径
		String providerPath = getProviderPath(url);
		// 在zk服务器上删除相应路径
		zk.delete(providerPath, -1);
	}

	/**
	 * zookeeper操作：
	 * 1. getChildren /appKey/service/providers/, watcher 获取IP节点列表
	 * 2. create /appKey/service/consumers/IP 注册信息到IP节点
	 * 
	 * watcher监视IP节点列表的变化
	 * @param url service url
	 * @param listener
	 * @throws Exception 
	 */
	@Override
	public void subscribe(URL url, NotifyListener listener) throws Exception {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(url.getPath());
		Preconditions.checkNotNull(listener);
		// 生成provider路径
		String providerPath = getProviderPath(url);
		// 获取IP节点列表
		// 新建watcher监听节点目录变化
		Watcher watcher = new ChildrenWatcher(providerPath, listener);
		// 获取IP节点列表
		List<String> paths = zk.getChildren(providerPath, watcher);
		// 通知listener
		doNotify(listener, paths);
		// 注册信息到IP节点
		// 生成consumer路径
		String consumerPath = getConsumerPath(url);
		// 创建consumer节点
		zk.create(consumerPath, "".getBytes(), null, CreateMode.EPHEMERAL);
	}
	/**
	 * 实际执行listener回调
	 * @param listener 
	 * @param paths 获取的zk路径信息
	 */
	private void doNotify(NotifyListener listener, List<String> paths) {
		// 新建不可变url列表的builder
		ImmutableList.Builder<URL> listBuilder = ImmutableList.builder();
		// 将所有获取的path全部转化成url并放入列表
		paths.forEach(p -> {
			URL u = URL.builder().str(p).build();
			listBuilder.add(u);
		});
		// 新建url缓存
		List<URL> urls = listBuilder.build();
		// 回调监听器
		listener.notify(urls);
	}
	/**
	 * zookeeper操作:
	 * 1. delete /appkey/service/consumer/IP
	 * @param url service url
	 * @param listener
	 * @throws Exception 
	 */
	@Override
	public void unsubscribe(URL url, NotifyListener listener) throws Exception {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(url.getPath());
		Preconditions.checkNotNull(listener);
		// 生成consumer路径
		String consumerPath = getConsumerPath(url);
		// TODO 版本问题 删除zk上的路径
		zk.delete(consumerPath, -1);
		// 传入空列表，回调监听器
		listener.notify(ImmutableList.of());
	}

	/**
	 * zookeeper操作：
	 * 1. getChildren /appKey/service/providers/
	 * @param url
	 * @return
	 * @throws Exception 
	 */
	@Override
	public List<URL> lookup(URL url) throws Exception {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(url.getPath());
		// 生成provider路径
		String providerPath = getProviderPath(url);
		// 获取IP节点列表
		List<String> paths = zk.getChildren(providerPath, false);
		// 新建不可变url列表的builder
		ImmutableList.Builder<URL> listBuilder = ImmutableList.builder();
		// 将所有获取的path全部转化成url并放入列表
		paths.forEach(p -> {
			URL u = URL.builder().str(p).build();
			listBuilder.add(u);
		});
		// 新建url缓存
		List<URL> urls = listBuilder.build();
		return urls;
	}
	
	/**
	 * 形如: /appKey/service/providers/IP
	 * @param url
	 * @return
	 */
	private String getProviderPath(URL url) {
		Preconditions.checkNotNull(url);
		// 获取path
		String path = url.getPath();
		// path必须存在且以providers结尾
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.endsWith(ProviderConstants.PROVIDERS));;
		// 正确格式 /appKey/service/providers/ 至少有三个分隔
		StringBuilder builder = new StringBuilder(path);
		// 组装/appKey/service/providers/IP格式
		builder.append(URL.PATH_SPLIT)
		       .append(url.connectString());
		return builder.toString();
	}
	
	/**
	 * 形如: /appKey/service/consumers/IP
	 * @param url
	 * @return
	 */
	private String getConsumerPath(URL url) {
		Preconditions.checkNotNull(url);
		// 获取path
		String path = url.getPath();
		// path必须存在且以consumes结尾
		Preconditions.checkNotNull(path);
		Preconditions.checkArgument(path.endsWith(ConsumerConstants.CONSUMERS));;
		// 正确格式 /appKey/service/consumers/ 至少有三个分隔
		StringBuilder builder = new StringBuilder(path);
		// 组装/appKey/service/consumers/IP格式
		builder.append(URL.PATH_SPLIT)
		       .append(url.connectString());
		return builder.toString();
	}
}
