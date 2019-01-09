package rpc.redis;

import java.util.List;

import redis.clients.jedis.Jedis;

/**
 * @author bird redis服务列表操作管理
 */
public class RedisServiceManager {
	private static final RedisServiceManager instance = new RedisServiceManager();
	
	private RedisServiceManager() {}
	
	public static RedisServiceManager getInstance() {
		return instance;
	}
	/**
	 * @param serviceInterface
	 * @return
	 */
	public List<String> getServiceList(Class<?> serviceInterface) {
		Jedis conn = RedisPoolUtil.getPool().getResource();
		String keyName = RedisKeyUtil.getServiceListKey(serviceInterface);
		try {
			List<String> serviceList = conn.lrange(keyName, 0, -1);
			return serviceList;
		} finally {
			conn.close();
		}
	}
	
	public void updateServiceList(List<String> updateData) {
		//此处应当更新redis数据
		//
	}
	
//	public void updateServiceListFlag() {}
}
