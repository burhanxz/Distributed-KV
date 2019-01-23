package rpc.loadBalance;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;

import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.URL;

/**
 * 利用一致性hash算法来选择节点
 * @author bird
 *
 */
public class SourceURLHashLB implements LoadBalance{
	/**
	 * 利用有序的map存放虚拟节点
	 */
	private static ThreadLocal<TreeMap<Integer, Invoker<?>>> maps = new ThreadLocal<TreeMap<Integer, Invoker<?>>>() {
		@Override
		protected TreeMap<Integer, Invoker<?>> initialValue() {
			// 新建treeMap
			return new TreeMap<>();
		}
		@Override
		public TreeMap<Integer, Invoker<?>> get() {
			// 重载get(),在获取map之前清空
			TreeMap<Integer, Invoker<?>> map = super.get();
			map.clear();
			return map;
		}
	};
	/**
	 * 每个物理节点对应的虚拟节点数目
	 */
	private final static int VIRTUAL_NODES_PER_NODE = 1 << 16;
	/**
	 * BKDR哈希常量
	 */
	private final static long BKDR_HASH_SEED = 1313131;

	@SuppressWarnings("unchecked")
	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		SortedMap<Integer, Invoker<?>> virtualNodes = maps.get();
		// 对服务列表进行遍历，物理节点扩散成虚拟节点加入哈希环
		invokers.forEach(invoker -> {
			URL remoteUrl = invoker.getUrl();
			// 创建virtualNodeSize个虚拟节点，均匀散布在大小为HASH_SIZE的hash环上
			IntStream.range(0, VIRTUAL_NODES_PER_NODE).forEach(i -> {
				String ip = remoteUrl.connectString();
				// 通过键和索引值获取哈希值，性能好的hash函数能保证映射后分布均匀
				int hashCode = (int) hash(ip, String.valueOf(i).intern());
				// 将获取的哈希值和物理节点放入虚拟节点map中
				virtualNodes.put(hashCode, invoker);
			});
		});
		// 本地url映射到哈希环上
		String localIp = url.connectString();
		int location = (int) hash(localIp);
		Invoker<?> node = null;
		if (location > virtualNodes.lastKey()) {
			//location超过范围则取第一个节点
			node = virtualNodes.get(virtualNodes.firstKey());
		} else {
			//获取子map, 顺时针最近节点是子map的第一个节点
			SortedMap<Integer, Invoker<?>> subMap = virtualNodes.tailMap(location);
			node = subMap.get(subMap.firstKey());
		}
		Preconditions.checkNotNull(node);
		return (Invoker<T>) node;
	}
	/**
	 * BKDR 哈希算法
	 * @param key
	 * @param append
	 * @return
	 */
	private int hash(String key, String append) {
		long hash = 0;
		for (int i = 0; i < key.length() + append.length(); i++) {
			hash = (hash * BKDR_HASH_SEED) + i < key.length() ? key.charAt(i) : append.charAt(i);
		}
		return (int) hash;
	}
	
	/**
	 * @param key
	 * @return
	 */
	private int hash(String key) {
		long hash = 0;
		for (int i = 0; i < key.length(); i++) {
			hash = (hash * BKDR_HASH_SEED) + key.charAt(i);
		}
		return (int) hash;
	}
}
