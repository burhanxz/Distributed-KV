package rpc.impl;

import rpc.Cluster;
import rpc.Directory;
import rpc.Invoker;

/**
 * 适用于非幂等性系统的cluster实现
 * @author bird
 *
 */
public class FailfastCluster implements Cluster{

	@Override
	public <T> Invoker<T> join(Directory<T> directory) {
		// 返回适用于failfast规则的Invoker
		return new FailfastClusterInvoker<>(directory);
	}

}
