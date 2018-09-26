package cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.SortedMap;

public class IPHashCluster extends HashCluster {
	protected IPHashCluster() {}
	@Override
	public String getNode(Class<?> serviceInterface) {
		//初始化一致性hash环上的虚拟节点
		addNodes(serviceInterface);
		
		try {
			//获取本机IP
			InetAddress ip = InetAddress.getLocalHost();
			//获取本机host
			String host = ip.getHostAddress();
			//将本机IP映射到hash环上的具体位置
			int location = hash(host) % HASH_SIZE;
			//服务器主机节点
			ServerNode node = null;
			//如果在hash环上的映射位置超过虚拟节点的最大hash值
			if (location > virtualNodes.lastKey()) {
				//顺时针最近节点即虚拟节点的第一个节点
				node = virtualNodes.get(virtualNodes.firstKey());
			} else {
				//获取子map
				//顺时针最近节点是子map的第一个节点
				SortedMap<Integer, ServerNode> subMap = virtualNodes.tailMap(location);
				node = subMap.get(subMap.firstKey());
			}
			//返回指定的服务器主机地址
			return node.toString();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

}
