package zookeeper;

public interface Monitor {
	/**
	 * 初始化app和service所共同确定的服务的服务列表
	 * 在初次使用某服务的时候触发
	 * @param redisKey
	 */
	public void initServiceList(String app, String service);
}
