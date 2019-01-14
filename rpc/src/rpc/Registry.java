package rpc;

import java.util.List;

/**
 * 注册中心
 * @author bird
 *
 */
public interface Registry extends Node{
	/**
	 * 注册信息到注册中心
	 * @param url
	 */
	public void register(URL url);
	/**
	 * 取消注册
	 * @param url
	 */
	public void unregister(URL url);
	/**
	 * 订阅服务，等待注册中心push数据
	 * @param url
	 * @param listener 监听器，监听注册中心信息改变
	 */
	public void subscribe(URL url, NotifyListener listener);
	/**
	 * 取消订阅服务，等待注册中心push数据
	 * @param url
	 * @param listener 监听器，监听注册中心信息改变
	 */
	public void unsubscribe(URL url, NotifyListener listener);
	/**
	 * 主动从注册中心pull数据
	 * @param url
	 * @return 满足匹配的url
	 */
	public List<URL> lookup(URL url);
}
