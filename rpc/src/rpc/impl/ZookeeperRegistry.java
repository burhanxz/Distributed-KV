package rpc.impl;

import java.io.IOException;
import java.util.List;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import rpc.NotifyListener;
import rpc.Registry;
import rpc.URL;

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
	private static final int ZK_CONNECT_TIMEOUT = 1000;
	private ZooKeeper zk;
	private final Lock mutex = new ReentrantLock();
	private final Condition connCondition = mutex.newCondition(); 
	/**
	 * 注册中心url
	 */
	private final URL registryUrl;
	private volatile boolean isAvailable; 
	public ZookeeperRegistry(URL registryUrl) throws IOException {
		this.registryUrl = registryUrl;
		init();
	}
	/**
	 * 初始化注册中心
	 * @throws IOException zookeeper连接异常
	 */
	private void init() throws IOException {
		// 获取zookeeper连接IP
		String connectString = registryUrl.connectString();
		mutex.lock();
		try {
			// 新建zookeeper连接
			zk = new ZooKeeper(connectString, ZK_CONNECT_TIMEOUT, new ConnectionWatcher());
			// 等待连接完成
			connCondition.await();
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
						// 唤醒
						connCondition.signalAll();
					} finally {
						mutex.unlock();
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
	public void register(URL url) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unregister(URL url) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * zookeeper操作：
	 * 1. getChildren /appKey/service/providers/, watcher 获取IP节点列表
	 * 2. getData IP/, no watcher 获取service provider信息
	 * 3. create /appKey/service/consumers/IP 注册信息到IP节点
	 * 4. setData IP/ 设置消费者信息
	 * 
	 * watcher监视IP节点列表的变化
	 * @param url
	 * @param listener
	 */
	@Override
	public void subscribe(URL url, NotifyListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * zookeeper操作:
	 * 1. delete /appkey/service/consumer/IP
	 * @param url
	 * @param listener
	 */
	@Override
	public void unsubscribe(URL url, NotifyListener listener) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * zookeeper操作：
	 * 1. getChildren /appKey/service/providers/
	 * @param url
	 * @return
	 */
	@Override
	public List<URL> lookup(URL url) {
		// TODO Auto-generated method stub
		return null;
	}

}
