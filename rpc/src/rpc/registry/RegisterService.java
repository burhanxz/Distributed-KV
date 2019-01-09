package rpc.registry;

/**
 * 代表注册服务的接口
 * @author bird
 *
 */
public interface RegisterService {
	/**
	 * 将注册信息存入ZooKeeper
	 * @param registerInfo 注册信息
	 */
	public void register(RegisterInfo registerInfo);
}
