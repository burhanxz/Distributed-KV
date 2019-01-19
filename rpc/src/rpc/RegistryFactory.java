package rpc;

import java.io.IOException;

/**
 * 注册中心工厂
 * @author bird
 *
 */
public interface RegistryFactory {
	/**
	 * 根据注册中心的url获取注册中心客户端
	 * @param url
	 * @return
	 * @throws IOException 获取zk连接异常
	 */
	public Registry getRegistry(URL url) throws IOException;
}
