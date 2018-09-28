package cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import com.alibaba.fastjson.JSONObject;

import host.ServerNode;
import redis.RedisServiceManager;
import registry.RegisterInfo;

public abstract class HashCluster implements Cluster {
	protected final static int HASH_SIZE = 1 << 30;
	/**
	 * 存放服务器节点
	 */
	protected List<ServerNode> nodes = new ArrayList<>();
	/**
	 * 利用有序的map存放虚拟节点
	 */
	protected SortedMap<Integer, ServerNode> virtualNodes = new TreeMap<>();

	private String getHostKey(RegisterInfo info) {
		return info.toString();
	};

	/**
	 * BKDR 哈希算法
	 * 
	 * @param key
	 * @return
	 */
	protected int hash(String key) {
		// 种子
		long seed = 131; // 31 131 1313 13131 131313 etc..
		long hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash = (hash * seed) + key.charAt(i);
		}
		return (int) hash;
	}

	/**
	 * 将redis中的服务列表取出后，得到物理节点和虚拟节点，并将虚拟节点均匀分布在hash环上
	 * 
	 * @param serviceInterface
	 *            服务接口类
	 */
	protected void addNodes(Class<?> serviceInterface) {
		// 获取RedisServiceManager
		RedisServiceManager manager = RedisServiceManager.getInstance();
		// 从redis获取服务列表
		List<String> serviceList = manager.getServiceList(serviceInterface);
		// 计算每个物理节点对应的虚拟节点的数量
		int virtualNodeSize = HASH_SIZE / serviceList.size();
		// 对服务列表进行遍历
		serviceList.forEach(e -> {
			// 将字符串解析成RegisterInfo对象
			RegisterInfo info = JSONObject.parseObject(e, RegisterInfo.class);
			String host = info.getHost();
			int port = info.getPort();
			// 通过解析出来的RegisterInfo对象获取键
			String key = getHostKey(info);
			// 根据IP创建物理节点对象
			ServerNode node = new ServerNode(host, port);
			// 将物理节点对象放入物理节点列表中
			nodes.add(node);
			// 创建virtualNodeSize个虚拟对象，均匀散布在大小为HASH_SIZE的hash环上
			IntStream.range(0, virtualNodeSize).forEach(i -> {
				// 通过键和索引值获取哈希值，性能好的hash函数能保证映射后分布均匀
				int hashCode = (int) hash(key + i);
				// 将获取的哈希值和物理节点放入虚拟节点map中
				virtualNodes.put(hashCode, node);
			});

		});
		//
	}

}
