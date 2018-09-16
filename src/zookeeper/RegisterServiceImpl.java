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
	/**
	 * 锁的参数，等连接成功这一事件完成时释放锁
	 */
	private static int WAIT_FOR_CONNECT_SUCCESS = 1;
	/**
	 * zooKeeper连接对象
	 */
	private ZooKeeper zk;
	/**
	 * 执行connect方法时阻塞，收到ZooKeeper响应时释放
	 */
	private CountDownLatch latch = new CountDownLatch(WAIT_FOR_CONNECT_SUCCESS);
	/**
	 * 日志对象
	 */
	private Logger logger = Log.logger;
	/**
	 *  创建ZooKeeper目录时所设数据，为空字符串的比特数组
	 */
	private static final byte[] DIRECTORY_DATA = "".getBytes();
	/**
	 * servers目录是存放多个server信息的通用目录
	 */
	private static final String SERVER_REGISTER_DIRECTORY = "servers";
	/**
	 * 存放注册信息的叶节点名称"server"
	 */
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

	/* 
	 * @see zookeeper.RegisterService#register(zookeeper.RegisterInfo)
	 */
	@Override
	public void register(RegisterInfo registerInfo) {
		try {
			//判断application目录是否存在并设置
			//application目录名是RegisterInfo类的application
			String applicationPath = "/" + registerInfo.getApplication();
			Stat applicationStat = zk.exists(applicationPath, null);
			if(applicationStat == null) {
				//创建/application/
				zk.create(applicationPath, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
			}
			//判断service目录是否存在并设置
			//service目录名是RegisterInfo类的InterfaceClazz的类名
			String servicePath = applicationPath + "/" + registerInfo.getInterfaceClazz().getName();
			Stat serviceStat = zk.exists(servicePath, null);
			if(serviceStat == null) {
				//创建/application/service/ 
				zk.create(servicePath, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
				//创建/application/service/servers
				zk.create(servicePath + "/" + SERVER_REGISTER_DIRECTORY, DIRECTORY_DATA, null, CreateMode.PERSISTENT);
			}
			//server目录形如server1,server2..
			String serverPath = servicePath + "/" + REGISTER_NODE_NAME;
			//将RegisterInfo序列化后存入server目录
			byte[] registerData = registerInfo.toString().getBytes();
			//创建server目录并存入序列化后的信息
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
