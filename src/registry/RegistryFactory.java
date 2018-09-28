package registry;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author bird 工厂类，用于产生IRegistry对象
 */
public class RegistryFactory {
	/* IRegistryFactory的单例类对象 */
	private static final RegistryFactory instance = new RegistryFactory();

	/* 私有构造器，单例类用 */
	private RegistryFactory() {
	}

	/**
	 * @return 获取单例类对象
	 */
	public static RegistryFactory getInstance() {
		return instance;
	}

	/* 用于存放已生成的IRegistry */
	private Map<InetSocketAddress, Registry> iRegistryMap = new ConcurrentHashMap<>();

	/**
	 * @param address
	 *            根据地址来新建或取出已有的IRegistry
	 * @return IRegistry对象
	 */
	public Registry getZooKeeperRegistry() {



			Registry iRegistry = new ZooKeeperRegistryImpl();


			return iRegistry;


	}
}
