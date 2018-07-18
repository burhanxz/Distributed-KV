package com.xuzhong.rpc.server;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class RegisterService {
	Logger logger = Logger.getLogger(this.getClass());
	ZooKeeper zk = null;

	public void connect(String address) {
		try {
			zk = new ZooKeeper(address, 2000, null);

		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("connected!");
	}

	// 注册主机地址到某服务节点下
	public void register(String service, String hostport) {
		try {
			String servicePath = "/" + service;
			Stat st = zk.exists(servicePath, false);
			if (st == null) {
				zk.create(servicePath, "".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
				// throw new UnsupportedOperationException("this service is now not exist");
			}
			String actulPath = zk.create(servicePath + "/server", hostport.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.EPHEMERAL_SEQUENTIAL);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public void printPath() {
		try {
			List<String> list = zk.getChildren("/", null);
			logger.info(list);
			for (String s : list) {
				logger.info(zk.getChildren("/" + s, null));
			}
			// logger.info(new String(zk.getData("/NameService/server0000000011", null, new
			// Stat())));
		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// 测试
	public static void main(String[] args) {
		RegisterService r = new RegisterService();
		r.connect("127.0.0.1:3000");
		r.register("NameService", "192.168.197.186:8080");
		r.printPath();
	}

}
