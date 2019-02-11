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
	 * @throws Exception 
	 */
	public void register(URL url) throws Exception;
	/**
	 * 取消注册
	 * @param url
	 * @throws Exception 
	 */
	public void unregister(URL url) throws Exception;
	/**
	 * 订阅服务，等待注册中心push数据
	 * @param url
	 * @param listener 监听器，监听注册中心信息改变
	 * @throws Exception 
	 */
	public void subscribe(URL url, NotifyListener listener) throws Exception;
	/**
	 * 取消订阅服务，等待注册中心push数据
	 * @param url
	 * @param listener 监听器，监听注册中心信息改变
	 * @throws Exception 
	 */
	public void unsubscribe(URL url, NotifyListener listener) throws Exception;
	/**
	 * 主动从注册中心pull数据
	 * @param url
	 * @return 满足匹配的url
	 * @throws Exception 
	 */
	public List<URL> lookup(URL url) throws Exception;
}
