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
	 * @param invokers
	 * @param url
	 * @param invocation
	 * @return
	 */
	public <T> List<Invoker<T>> select(List<Invoker<T>> invokers, URL url, Invocation invocation);
}
