package zookeeper;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bird 工厂类，用于产生IRegistry对象
 */
public class IRegistryFactory {
	/* IRegistryFactory的单例类对象 */
	private static final IRegistryFactory instance = new IRegistryFactory();

	/* 私有构造器，单例类用 */
	private IRegistryFactory() {
	}

	/**
	 * @return 获取单例类对象
	 */
	public static IRegistryFactory getInstance() {
		return instance;
	}

	/* 用于存放已生成的IRegistry */
	private Map<InetSocketAddress, IRegistry> iRegistryMap = new ConcurrentHashMap<>();

	/**
	 * @param address
	 *            根据地址来新建或取出已有的IRegistry
	 * @return IRegistry对象
	 */
	public IRegistry getZkRegistry(InetSocketAddress address) {

		if (!iRegistryMap.containsKey(address)) {

			IRegistry iRegistry = new ZooKeeperIRegistryImpl(address);

			IRegistry iRegistryInMap = iRegistryMap.putIfAbsent(address, iRegistry);

			if (iRegistryInMap != null) {
				iRegistry = iRegistryInMap;
			}
			// 让注册机运行
			((ZooKeeperIRegistryImpl) iRegistry).run();

			return iRegistry;

		}

		return iRegistryMap.get(address);

	}
}
