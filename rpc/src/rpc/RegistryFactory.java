package rpc;

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
	 */
	public Registry getRegistry(URL url);
}
