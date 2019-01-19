package rpc.impl;

import rpc.Cluster;
import rpc.Directory;
import rpc.Invoker;

/**
 * 适用于非幂等性系统的cluster实现
 * @author bird
 *
 */
public class FailfastClusterImpl implements Cluster{

	@Override
	public <T> Invoker<T> join(Directory<T> directory) {
		// TODO Auto-generated method stub
		return null;
	}

}
