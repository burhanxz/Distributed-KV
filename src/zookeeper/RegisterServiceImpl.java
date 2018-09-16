package zookeeper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZKUtil;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import facet.Log;

/**
 * 执行注册服务的动作
 * @author bird
 *
 */
public class RegisterServiceImpl implements RegisterService, Watcher{
	private static int WAIT_FOR_CONNECT_SUCCESS = 1;
	private ZooKeeper zk;
	private CountDownLatch latch = new CountDownLatch(WAIT_FOR_CONNECT_SUCCESS);
	private Logger logger = Log.logger;
	/**
	 *  创建ZooKeeper目录时所设数据，为空字符串的比特数组
	 */
	private static final byte[] DIRECTORY_DATA = "".getBytes();
	private static final String SERVER_REGISTER_DIRECTORY = "servers";
	private static final String REGISTER_NODE_NAME = "server";
	/* 
	 * @see zookeeper.RegisterService#connect(java.lang.String, int)
	 */
	@Override
	public void connect(final String host,final int port) {
		try {
			//异步过程
			//创建ZooKeeper客户端，连接ZooKeeper服务器
			zk = new ZooKeeper(host,port,this);
			//阻塞，直到process方法中释放锁
			latch.await();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void register(RegisterInfo registerInfo) {
		try {
			String applicationPath = "/" + registerInfo.getApplication();
			Stat applicationStat = zk.exists(applicationPath, null);
			if(applicationStat == null) {
				zk.create(applicationPath, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
			}
			
			String servicePath = applicationPath + "/" + registerInfo.getInterfaceClazz().getName();
			Stat serviceStat = zk.exists(servicePath, null);
			if(serviceStat == null) {
				zk.create(servicePath, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
				zk.create(servicePath + "/" + SERVER_REGISTER_DIRECTORY, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
			}
			
			String serverPath = servicePath + "/" + REGISTER_NODE_NAME;
			byte[] registerData = registerInfo.toString().getBytes();
			zk.create(serverPath, registerData, null, CreateMode.EPHEMERAL_SEQUENTIAL);
			
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void process(WatchedEvent event) {
		//获取事件状态
		//SyncConnected状态，表示和全体ZooKeeper服务器连接正常
		if(event.getState() == Watcher.Event.KeeperState.SyncConnected) {
			//获取事件类型
			EventType eventType = event.getType();
			switch(eventType) {
			//None事件可以表明连接成功
			//有争议
			case None:
				logger.info("connected to ZooKeeper!");
				//连接成功，结束阻塞的connect()方法
				latch.countDown();
				break;
			}
		}
		
	}


	
//	Logger logger = Logger.getLogger(this.getClass());
//	ZooKeeper zk = null;
//	
//	public void connect(String address) {
//		try {
//			zk = new ZooKeeper(address, 2000, null);
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		logger.info("connected!");
//	}
//
//	// 注册主机地址到某服务节点下
//	public void register(String service, String hostport) {
//		try {
//			String servicePath = "/" + service;
//			Stat st = zk.exists(servicePath, false);
//			if (st == null) {
//				zk.create(servicePath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
//				// throw new UnsupportedOperationException("this service is now not exist");
//			}
//			String actulPath = zk.create(servicePath + "/server", hostport.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
//					CreateMode.EPHEMERAL_SEQUENTIAL);
//		} catch (Exception e) {
//			e.printStackTrace();
//			return;
//		}
//	}
//
//	public void printPath() {
//		try {
//			List<String> list = zk.getChildren("/", null);
//			logger.info(list);
//			for (String s : list) {
//				logger.info(zk.getChildren("/" + s, null));
//			}
//			// logger.info(new String(zk.getData("/NameService/server0000000011", null, new
//			// Stat())));
//		} catch (KeeperException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//
//	// 测试
//	public static void main(String[] args) {
//		RegisterServiceImpl r = new RegisterServiceImpl();
//		r.connect("127.0.0.1:3000");
//		r.register("NameService", "192.168.197.186:8080");
//		r.printPath();
//	}

}
