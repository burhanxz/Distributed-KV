package rpc.impl;

import java.util.List;

import rpc.Directory;
import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.Result;

public class FailfastClusterInvoker<T> extends ClusterInvoker<T> {
	private Class<T> type;
	private Directory<T> directory;
	private LoadBalance loadBalance;
	public FailfastClusterInvoker(Directory<T> directory) {
		// TODO
		this.directory = directory;
		this.type = directory.getInterface();
	}
	@Override
	public Class<T> getInterface() {
		return type;
	}

	@Override
	public Result invoke(Invocation invocation) {
		List<Invoker<T>> invokers = directory.list(invocation);
		List<Invoker<T>> invokersAfterLoadBalance = loadBalance.select(invokers, directory.getUrl(), invocation);
		//TODO
		return null;
	}

}
