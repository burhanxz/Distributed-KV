package zookeeper;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.List;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import com.alibaba.fastjson.JSON;

import org.apache.zookeeper.Watcher.Event.KeeperState;

import client.CommonInvokerFactory;
import client.Invoker;

public class RpcInvokeHandler implements InvocationHandler, Watcher, Runnable {
	/* ZooKeeper连接类对象 */
	private ZooKeeper zk = null;
	/* 标识本类对象所代表的监视器的活性 */
	private boolean isActive;
	/* 服务类 */
	private Class<?> serviceInterface;
	/* 存储从ZooKeeper服务器获取的服务列表 */
	private List<RegisterInfo> registerInfoList;

	/* 构造器 */
	public RpcInvokeHandler(ZooKeeper zk, Class<?> serviceInterface) {
		this.isActive = true;
		this.zk = zk;
		this.serviceInterface = serviceInterface;
	}

	/**
	 * 更新本地的服务列表
	 * 
	 * @throws KeeperException
	 * @throws InterruptedException
	 * @throws UnsupportedEncodingException
	 */
	public void getServerList() {
		// 服务列表
		List<String> list = null;
		try {
			// 从ZooKeeper服务器获取服务列表，完成后，设置监视器
			// 将本类对象作为监视器设置到ZooKeeper服务器
			//>>>>>>>>>>>>>
			list = zk.getChildren("/" + serviceInterface.getName(), this);
		} catch (KeeperException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 服务信息列表存入本地
		for(String registerInfoJson : list) {
			//将拉取的服务信息转化为RegisterInfo对象
			RegisterInfo registerInfo = JSON.parseObject(registerInfoJson, RegisterInfo.class);
			//将RegisterInfo存入serviceList
			registerInfoList.add(registerInfo);
		}
		
		// 测试代码
		// System.out.println("--------------------------------");
		// for (String server : serviceList) {
		// System.out.println(
		// serviceInterfaceName + " data: " + new String(zk.getData("/" +
		// serviceInterfaceName + "/" + server, false, null), "UTF-8"));
		// }
	}

	/*
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.WatchedEvent)
	 */
	@Override
	public void process(WatchedEvent event) {
		//如果ZooKeeper连接处于正常连接状态
		if (event.getState() == KeeperState.SyncConnected) {
			//对于事件类型
			switch (event.getType()) {
			// 服务器节点更新
			case NodeChildrenChanged:
				try {
					//如果服务节点更新，则主动拉取服务列表
					getServerList();
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			// 服务被删除了，则将整个监视器关闭，使得其失活
			case NodeDeleted:
				close();
				break;
			default:
				break;
			}
		}

	}

	// 回调，监视器运行
	@Override
	public void run() {
		
		getServerList();
		// 阻塞，等监视器关闭时则结束run
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	// 关闭监视器
	public void close() {
		synchronized (this) {
			this.notify();
		}
		// 使得监视器变为失活状态
		isActive = false;
	}

	// 检查是否活跃
	public boolean isActive() {
		return isActive;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 这里涉及到服务器选择策略的问题，待优化
		//从服务注册信息中获取host和port
		String host = registerInfoList.get(0).getHost();
		int port = registerInfoList.get(0).getPort();

		/* 服务器的注册与发现，和服务器的连接，是完全解耦的 */
		// 连接服务器，获取invoker
		Invoker invoker = CommonInvokerFactory.getInstance().get(new InetSocketAddress(host, port));
		Object result = invoker.invoke(serviceInterface, method, args);

		if (result instanceof Throwable)
			throw (Throwable) result;

		return result;
	}

}
