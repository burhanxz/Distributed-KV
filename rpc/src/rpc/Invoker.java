package rpc;

public interface Invoker<T> {
	/**
	 * 获取执行体中的接口类型
	 * @return
	 */
	public Class<T> getInterface();
	/**
	 * 执行体调用执行对象
	 * @param invocation 执行对象
	 * @return
	 * @throws Exception 数据传输异常
	 */
	public Result invoke(Invocation invocation) throws Exception;
}
