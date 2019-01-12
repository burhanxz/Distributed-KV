package rpc;

import java.util.List;

/**
 * 路由规则决定一次远程服务调用的目标服务器
 * @author bird
 *
 */
public interface Router extends Comparable<Router>{
	/**
	 * 获取目标服务器的url
	 * @return
	 */
	public URL getUrl();
	/**
	 * 通过一定的规则过滤出invokers的一个子集, 即路由选择，返回可用的invoker
	 * @param invokers 供选择的Invoker
	 * @param url 路由地址
	 * @param invocation invocation
	 * @return 可用的invoker
	 */
	public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation);
}
