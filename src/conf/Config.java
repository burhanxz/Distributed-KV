package conf;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author bird
 * （暂时）利用此类进行一些全局性配置 
 */
public class Config {
	/**
	 * 分布式锁的实现方式
	 */
	public static final String DISTRIBUTED_LOCK_IMPL = "RedisDistributedLock";
	/**
	 * ZooKeeper连接地址
	 */
	public static final String ZOOKEEPER_CONNECT_STRING = "127.0.0.1:2181";
	/**
	 * Redis连接地址
	 */
	public static final String REDIS_CONNECT_STRING = "127.0.0.1:6379";
	/**
	 * app名称
	 */
	public static final String APP_NAME = "app1";
	
	public static final String CLUSTER_STRATEGY = "IPHash";

}
