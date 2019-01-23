package rpc.loadBalance;

import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;

import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.URL;

/**
 * 随机选取节点
 * @author bird
 *
 */
public class RandomLB implements LoadBalance {
	private static Random random = new Random();
	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		Preconditions.checkNotNull(invokers);
		Preconditions.checkState(invokers.isEmpty());
		// 返回随机位置
		return invokers.get(random.nextInt(invokers.size()));
	}

}
