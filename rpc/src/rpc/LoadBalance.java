package rpc;

import java.util.List;

/**
 * 负载均衡策略
 * @author bird
 *
 */
public interface LoadBalance {
	/**
	 * 通过策略从服务列表中选择一个invoker
	 * @param invokers 执行体，代表远程物理节点
	 * @param url 本地url
	 * @param invocation
	 * @return
	 */
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation);
}
