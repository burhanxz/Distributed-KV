package rpc.impl;

import java.util.List;

import com.google.common.base.Preconditions;

import rpc.Directory;
import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.Result;
import rpc.loadBalance.RandomLB;
import rpc.model.RequestConstants;

/**
 * 适用于failfast机制集群的invoker
 * @author bird
 *
 * @param <T>
 */
public class FailfastClusterInvoker<T> extends ClusterInvoker<T> {
	/**
	 * 负载均衡器
	 */
	public FailfastClusterInvoker(Directory<T> directory) {
		super(directory);
	}
	@SuppressWarnings("unchecked")
	@Override
	public Result invoke(Invocation invocation) throws Exception{
		// directory列出注册中心拉取的服务列表、经过路由选择，筛选出的invoker
		List<Invoker<T>> invokers = directory.list(invocation);
		// 从invocation中选取负载均衡器
		String loadBalanceStrategy = invocation.getOptions().get(RequestConstants.LOAD_BALANCE);
		// 获取loadBalance类型
		Class<? extends LoadBalance> lbClazz = null;
		if(loadBalanceStrategy == null) {
			// 默认RandomLB
			lbClazz = RandomLB.class;
		}
		else {
			// 获取class对象
			Class<?> clazz = null;
			try {
				clazz = Class.forName(loadBalanceStrategy);
				Preconditions.checkArgument(clazz.isAssignableFrom(LoadBalance.class));
			} catch (ClassNotFoundException e) {
				// 获取loadBalance失败则使用默认值
				clazz = RandomLB.class;
				e.printStackTrace();
			}
			lbClazz = (Class<? extends LoadBalance>) clazz;
		}
		Preconditions.checkNotNull(lbClazz);
		// TODO 创建loadBalance实例
		LoadBalance loadBalance = null;
		// 捕获和调用无关的异常
		try {
			loadBalance = lbClazz.newInstance();
		} catch (InstantiationException e1) {
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			e1.printStackTrace();
		}
		Preconditions.checkNotNull(loadBalance);
		// 负载均衡选择出唯一invoker
		Invoker<T> invoker = loadBalance.select(invokers, directory.getUrl(), invocation);
		// invoker根据invocation执行调用
		try {
			Result result = invoker.invoke(invocation);
			return result;
		} catch (Exception e) {
			// 如果调用失败，不进行重试，直接抛出异常
			throw e;
		}
	}
}
