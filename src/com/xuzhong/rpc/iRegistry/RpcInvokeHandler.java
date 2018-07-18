package com.xuzhong.rpc.iRegistry;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import com.xuzhong.rpc.client.CommonInvokerFactory;
import com.xuzhong.rpc.client.Invoker;

public class RpcInvokeHandler implements InvocationHandler, Watcher, Runnable{
	private static ZooKeeper zk = null;
	private boolean isActive;
	private String service;
	private List<String> list;
	public RpcInvokeHandler(ZooKeeper zk, String service) {
		this.isActive = true;
		this.zk = zk;
		this.service = service;
	}
	
	//更新服务器列表
	public void getServerList() throws KeeperException, InterruptedException, UnsupportedEncodingException {
		List<String> serverList = zk.getChildren("/" + service, this);
		list = serverList;
		System.out.println("--------------------------------");
		for (String server : serverList) {
			System.out.println(
					service + " data: " + new String(zk.getData("/" + service + "/" + server, false, null), "UTF-8"));
		}
	}

	@Override
	public void process(WatchedEvent event) {
		if (event.getState() == KeeperState.SyncConnected) {
			switch (event.getType()) {
			//服务器节点更新了
			case NodeChildrenChanged:
				try {
					getServerList();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			//服务被删除了，则将整个监视器关闭，使得其失活
			case NodeDeleted:
				close();
				break;
			default:
				break;
			}
		}

	}
	
	//回调，监视器运行
	@Override
	public void run() {
		try {
			getServerList();
			//阻塞，等监视器关闭时则结束run
			synchronized (this) {
				this.wait();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// 关闭监视器
	public void close() {
		synchronized (this) {
			this.notify();
		}
		//使得监视器变为失活状态
		isActive = false;
	}
	
	//检查是否活跃
	public boolean isActive() {
		return isActive;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		//这里涉及到服务器选择策略的问题，待优化
		String[] hostPort = list.get(0).split(":");
		String host = hostPort[0];
		int port = Integer.valueOf(hostPort[1]);
		
		/*服务器的注册与发现，和服务器的连接，是完全解耦的*/
		//连接服务器，获取invoker
		Invoker invoker = CommonInvokerFactory.getInstance().get(new InetSocketAddress(host,port));
		Object result = invoker.invoke(Class.forName(service), method, args);
		
		if(result instanceof Throwable) 
			throw (Throwable)result;
		
		return result;
	}

}
