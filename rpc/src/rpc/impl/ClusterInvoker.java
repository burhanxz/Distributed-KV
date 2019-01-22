package rpc.impl;

import rpc.Directory;
import rpc.Invoker;

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
}
