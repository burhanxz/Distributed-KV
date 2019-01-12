package rpc;

public interface Cluster {
	/**
	 * 把invoker列表合并成一个虚拟的invoker
	 * @param directory
	 * @return
	 */
	public <T> Invoker<T> join(Directory<T> directory);
}
