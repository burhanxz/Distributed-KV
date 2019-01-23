package rpc;

/**
 * 集群，通过一定规则合并多个invoker为一个虚拟invoker
 * @author bird
 *
 */
public interface Cluster {
	/**
	 * 把invoker列表合并成一个虚拟的invoker
	 * @param directory
	 * @return
	 */
	public <T> Invoker<T> join(Directory<T> directory);
}
