package redis;

import conf.Config;

/**
 * @author bird
 * 根据信息拼装相应的redis键名
 */
public class RedisKeyUtil {
	/**
	 * redis分隔符
	 */
	private static final String REDIS_SEPARATOR = ":";
	/**
	 * 获取redis相应服务列表的Key
	 * @param interfaceClazz 接口类
	 * @return
	 */
	public static String getServiceListKey(Class<?> interfaceClazz) {
		StringBuilder stringBuilder = new StringBuilder();
		//app名称
		stringBuilder.append(Config.APP_NAME);
		//redis分隔符
		stringBuilder.append(REDIS_SEPARATOR);
		//获取接口类名
		stringBuilder.append(interfaceClazz.getName());
		return stringBuilder.toString();
	}
}
