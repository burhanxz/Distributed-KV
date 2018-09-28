package service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;

import cluster.Cluster;
import cluster.Clusters;
import cluster.Clusters.Strategy;
import conf.Config;
import connector.CommonInvokerFactory;
import connector.Invoker;
import redis.RedisKeyUtil;

/**
 * @author bird 动态代理核心实现
 */
public class RPCInvocationHandler implements InvocationHandler {
	/**
	 * 从全局配置中载入负载均衡策略
	 */
	private static Strategy CLUSTER_STRATEGY = Strategy.valueOf(Config.CLUSTER_STRATEGY);
	/**
	 * 服务类接口
	 */
	private Class<?> serviceInterface;
	/**
	 * 负载均衡器
	 */
	private Cluster cluster;

	public RPCInvocationHandler(Class<?> serviceInterface) {
		this(serviceInterface, CLUSTER_STRATEGY);
	}

	public RPCInvocationHandler(Class<?> serviceInterface, Strategy clusterStrategy) {
		this.serviceInterface = serviceInterface;
		//获取负载均衡器
		cluster = Clusters.newCluster(clusterStrategy);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		// 通过负载均衡器获取服务器节点
		String addressStr = cluster.getNode(serviceInterface);
		// 解析地址
		String[] addressStrs = addressStr.split(":");
		// 获取host和port
		String host = addressStrs[0];
		int port = Integer.valueOf(addressStrs[1]);

		/* 服务器的注册与发现，和服务器的连接，是完全解耦的 */
		// 连接服务器，获取invoker
		Invoker invoker = CommonInvokerFactory.getInstance().get(new InetSocketAddress(host, port));
		Object result = invoker.invoke(serviceInterface, method, args);
		// 如果获取的是Throwable类型，则抛出
		if (result instanceof Throwable)
			throw (Throwable) result;

		return result;

	}

}
