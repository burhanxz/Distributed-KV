package zookeeper;

import java.net.InetSocketAddress;

/**
 * @author bird
 * Registry类，用于产生代理对象，可进行代理对象
 * 它屏蔽了底层对ZooKeeper的访问	
 */
public interface Registry {
	
	/**
	 * @param serviceClazz 服务类
	 * @return 代理对象，可用于远程调用
	 */
	public <T> T lookup(Class<T> serviceClazz);
	
	/**
	 * @return 返回正在进行远程调用的IP地址和port
	 */
	public InetSocketAddress getRegisterAddress();
}
