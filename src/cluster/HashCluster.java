package cluster;

import java.util.ArrayList;
import java.util.List;

public abstract class HashCluster implements Cluster {
	/**
	 * 存放服务器节点
	 */
	protected List<ServerNode> nodes = new ArrayList<>();
	/**
	 * BKDR 哈希算法
	 * 
	 * @param key
	 * @return
	 */
	protected long hash(String key) {
		// 种子
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash = (hash * seed) + key.charAt(i);
		}
		return hash;
	}
	
	/**添加服务器节点到List
	 * @param host 主机
	 * @param port 端口
	 * @return
	 */
	protected boolean addNode(String host, int port) {
		return nodes.add(new ServerNode(host, port));
	}
	
	
}
