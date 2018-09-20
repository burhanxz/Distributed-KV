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
import init.ZooKeeperConnection;

/**
 * 执行注册服务的动作
 * 
 * @author bird
 *
 */
public class RegisterServiceImpl implements RegisterService{
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
	 * @see zookeeper.RegisterService#register(zookeeper.RegisterInfo)
	 */
	@Override
	public void register(RegisterInfo registerInfo) {
		ZooKeeper zk = ZooKeeperConnection.getInstance().getZooKeeper();
		try {
			// 判断application目录是否存在并设置
			// application目录名是RegisterInfo类的application
			String applicationPath = "/" + registerInfo.getApplication();
			Stat applicationStat = zk.exists(applicationPath, null);
			if (applicationStat == null) {
				// 创建/application/
				zk.create(applicationPath, DIRECTORY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
			}
			// 判断service目录是否存在并设置
			// service目录名是RegisterInfo类的InterfaceClazz的类名
			String servicePath = applicationPath + "/" + registerInfo.getInterfaceClazz().getName();
			Stat serviceStat = zk.exists(servicePath, null);
			if (serviceStat == null) {
				// 创建/application/service/
				zk.create(servicePath, DIRECTORY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				// 创建/application/service/servers
				zk.create(servicePath + "/" + SERVER_REGISTER_DIRECTORY, DIRECTORY_DATA, ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
			// server目录形如server1,server2..
			String serverPath = servicePath + "/" + SERVER_REGISTER_DIRECTORY + "/" + REGISTER_NODE_NAME;
			// 将RegisterInfo序列化后存入server目录
			byte[] registerData = registerInfo.toString().getBytes();
			// 创建server目录并存入序列化后的信息
			zk.create(serverPath, registerData, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//测试代码
	public static void main(String[] args) {
		RegisterInfo info = new RegisterInfo();
		info.setApplication("app1");
		info.setHost("3.3.3.3");
		info.setPort(9999);
		info.setInterfaceClazz(Foo.class);
		info.setImplementClazz(new FooImpl());
		info.setTimeout(1000);
		info.setWeight(0.3f);
		RegisterService r = new RegisterServiceImpl();
		r.register(info);
		while (true) {

		}
		// ZooKeeper服务器上运行结果
		// {"@type":"zookeeper.RegisterInfo","application":"app1","host":"3.3.3.3","implementClazz":{"@type":"zookeeper.FooImpl"},"interfaceClazz":"zookeeper.Foo","port":9999,"timeout":1000,"weight":0.3}
		// cZxid = 0x1b
		// ctime = Mon Sep 17 10:02:38 CST 2018
		// mZxid = 0x1b
		// mtime = Mon Sep 17 10:02:38 CST 2018
		// pZxid = 0x1b
		// cversion = 0
		// dataVersion = 0
		// aclVersion = 0
		// ephemeralOwner = 0x165e535f3a90005
		// dataLength = 192
		// numChildren = 0

	}
}
//测试
interface Foo {
	String get();
}
//测试
class FooImpl implements Foo {

	@Override
	public String get() {
		// TODO Auto-generated method stub
		return null;
	}

}