package zookeeper;

/**
 * 代表注册服务的接口
 * @author bird
 *
 */
public interface RegisterService {
	/**
	 * 连接ZooKeeper，阻塞直到连接完成
	 * @param host ZooKeeper地址
	 * @param port ZooKeeper端口
	 */
	public void connect(final String host,final int port);
	
	/**
	 * 将注册信息存入ZooKeeper
	 * @param registerInfo 注册信息
	 */
	public void register(RegisterInfo registerInfo);
}
