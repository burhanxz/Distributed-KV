package zookeeper;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 * 监视ZooKeeper服务端节点信息变化，并且存入本地redis，作为本地服务列表
 * @author bird
 *
 */
public class ZooKeeperMonitor implements Monitor,Watcher{
	
	@Override
	public void initServiceList(String app, String service) {
		
	}
	@Override
	public void process(WatchedEvent event) {
		
	}



}
