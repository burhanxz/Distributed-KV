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
	 * 
     * 1. 当用户调用refer()所返回的Invoker对象的invoke()方法时，协议需相应执行同URL远端export()传入的Invoker对象的invoke()方法。
     * 2. refer()返回的Invoker由协议实现，协议通常需要在此Invoker中发送远程请求。
     * 3. 当url中有设置check=false时，连接失败不能抛出异常，需内部自动恢复。
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
