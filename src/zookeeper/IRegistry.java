package zookeeper;

import java.net.InetSocketAddress;

/**
 * @author bird
 * IRegistry类，用于产生代理对象，可进行代理对象	
 */
public interface IRegistry {
	
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
