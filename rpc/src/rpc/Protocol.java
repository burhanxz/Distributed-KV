package rpc;

public interface Protocol {
	/**
	 * 暴露远程服务
	 * @param invoker 服务的执行体
	 * @return 暴露服务的引用
	 */
	public <T> Exporter<T> export(Invoker<T> invoker);
	/**
	 * 引入远程服务
	 * @param type 服务的类型
	 * @param url 远程服务的URL地址
	 * @return 服务的本地代理
	 */
	public <T> Invoker<T> refer(Class<T> type, URL url);
	/**
	 * 销毁协议
	 */
	public void destroy();
}
