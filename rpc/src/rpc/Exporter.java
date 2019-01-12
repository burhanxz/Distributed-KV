package rpc;

public interface Exporter<T> {
	/**
	 * 获取暴露的远程服务的执行体
	 * @return
	 */
	public Invoker<T> getInvoker();
	/**
	 * 结束暴露远程服务
	 */
	public void unexport();
}
