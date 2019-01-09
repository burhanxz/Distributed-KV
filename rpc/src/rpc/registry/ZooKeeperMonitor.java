package rpc.registry;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 监视ZooKeeper服务端节点信息变化，并且存入本地redis，作为本地服务列表
 * @author bird
 *
 */
public class ZooKeeperMonitor implements Monitor,Watcher{
	private static final ZooKeeperMonitor instance = new ZooKeeperMonitor();
	private ZooKeeperMonitor() {}
	public static ZooKeeperMonitor getInstance() {
		return instance;
	}
	@Override
	public void initServiceList(Class<?> serviceInterfaceClazz) {
		
	}
	@Override
	public void process(WatchedEvent event) {
		
	}



}
