package bootstrap;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import facet.Log;

import org.apache.zookeeper.Watcher.Event.EventType;

public class ZooKeeperConnection implements Watcher {
	/* ZooKeeperConnection的实例对象 */
	private static ZooKeeperConnection instance = new ZooKeeperConnection();
	/**
	 * 锁的参数，等连接成功这一事件完成时释放锁
	 */
	private static int WAIT_FOR_CONNECT_SUCCESS = 1;
	/**
	 * 执行connect方法时阻塞，收到ZooKeeper响应时释放
	 */
	private static final CountDownLatch latch = new CountDownLatch(WAIT_FOR_CONNECT_SUCCESS);
	/* ZooKeeper参数，会话时间限制 */
	private static final int SESSION_TIME = 2000;
	/**
	 * ZooKeeper连接对象
	 */
	private ZooKeeper zk = null;

	/* 私有构造器 */
	private ZooKeeperConnection() {
	}

	/**
	 * 连接ZooKeeper
	 */
	private void connect() {
		String connectString = "127.0.0.1:2181";
		try {
			Log.logger.info("开始连接ZooKeeper...");
			// 异步过程
			// 创建ZooKeeper客户端，连接ZooKeeper服务器
			zk = new ZooKeeper(connectString, SESSION_TIME, this);
			// 阻塞，直到process方法中释放锁
			latch.await();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 启动类调用，开始连接ZooKeeper
	 */
	void init() {
		if (zk == null) {
			connect();
		}
	}

	/**
	 * 获取ZooKeeperConnection对象
	 * 
	 * @return ZooKeeperConnection对象
	 */
	public static ZooKeeperConnection getInstance() {
		return instance;
	}

	/**
	 * 返回全局共有的ZooKeeper连接
	 * 
	 * @return 全局唯一ZooKeeper对象
	 */
	public ZooKeeper getZooKeeper() {
		if (zk == null)
			throw new RuntimeException("ZooKeeper尚未连接，框架初始化出错！");
		return zk;
	}

	@Override
	public void process(WatchedEvent event) {
		// 获取事件状态
		// SyncConnected状态，表示和全体ZooKeeper服务器连接正常
		if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
			// 获取事件类型
			EventType eventType = event.getType();
			switch (eventType) {
			// None事件可以表明连接成功
			// 有争议
			case None:
				Log.logger.info("connected to ZooKeeper!");
				// 连接成功，结束阻塞的connect()方法
				latch.countDown();
				break;
			}
		}
	}

}
