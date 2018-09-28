package registry;

public interface Monitor {
	/**
	 * 初始化app和service所共同确定的服务的服务列表
	 * 在初次使用某服务的时候触发
	 * @param 服务接口类
	 */
	public void initServiceList(Class<?> serviceInterfaceClazz);
}
