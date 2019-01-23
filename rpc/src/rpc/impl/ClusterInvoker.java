package rpc.impl;

import rpc.Directory;
import rpc.Invoker;
import rpc.URL;

/**
 * 用作cluster合并产生的集群invoker
 * @author bird
 *
 * @param <T> 接口类型
 */
public abstract class ClusterInvoker<T> implements Invoker<T>{
	/**
	 * 接口类型
	 */
	protected Class<T> type;
	/**
	 * invokers目录
	 */
	protected Directory<T> directory;
	public ClusterInvoker(Directory<T> directory) {
		this.directory = directory;
		this.type = directory.getInterface();
	}
	@Override
	public Class<T> getInterface() {
		return type;
	}
	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}
}
