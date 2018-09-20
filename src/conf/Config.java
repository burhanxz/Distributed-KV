package conf;

/**
 * @author bird
 * （暂时）利用此类进行一些全局性配置 
 */
public class Config {
	public static final String DISTRIBUTED_LOCK_IMPL = "RedisDistributedLock";
	public static final String ZOOKEEPER_CONNECT_STRING = "127.0.0.1:2181";
	public static final String APP_NAME = "app1";
}
