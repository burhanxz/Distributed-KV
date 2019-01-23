package rpc;

import java.util.Map;

public interface ProxyFactory {
	/**
	 * 获取代理对象，供客户端使用
	 * @param invoker
	 * @return
	 */
	public <T> T getProxy(Invoker<T> invoker, Map<String, String> Options);
	/**
	 * 获取invoker，供服务端使用
	 * @param proxy
	 * @param type
	 * @param url
	 * @return
	 */
	public <T> Invoker<T> getInvoker(T proxy, Class<T> type, URL url);
}
