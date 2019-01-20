package rpc.impl;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import rpc.Directory;
import rpc.Invocation;
import rpc.Invoker;
import rpc.NotifyListener;
import rpc.Protocol;
import rpc.Router;
import rpc.URL;

/**
 * 可被注册中心回调通知的invoker列表组件
 * @author bird
 *
 * @param <T> 接口类型
 */
public class NotifiedDirectroy<T> implements NotifyListener, Directory<T>{
	/**
	 * directory全局锁
	 */
	private final Lock mutex = new ReentrantLock();
	/**
	 * url列表状态锁
	 */
	private final Condition urlsCondition = mutex.newCondition();
	/**
	 * 服务提供者列表
	 */
	private List<URL> providerUrls;
	/**
	 * 协议实体，用于引入invoker
	 */
	private Protocol protocal;
	/**
	 * 路由过滤链
	 */
	private List<Router> routers;
	/**
	 * 接口类型
	 */
	private Class<T> type;
	/**
	 * 本地url
	 */
	private URL localUrl;
	/**
	 * 组件是否可用
	 */
	private volatile boolean isAvailable;
	public NotifiedDirectroy(Class<T> type, URL localUrl) {
		this.type = type;
		this.localUrl = localUrl;
		this.isAvailable = true;
	}
	@Override
	public URL getUrl() {
		return localUrl;
	}

	@Override
	public boolean isAvailable() {
		return isAvailable;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<T> getInterface() {
		return type;
	}

	@Override
	public List<Invoker<T>> list(Invocation invocation) {
		Preconditions.checkNotNull(invocation);
		Preconditions.checkState(isAvailable);
		// 过滤前后的Invoker列表
		List<Invoker<T>> invokersBeforeRoute = null;
		List<Invoker<T>> invokersAfterRoute = null;
		mutex.lock();
		try {
			// 如果url列表为空，则等待注册中心推送provider信息
			if(providerUrls.isEmpty()) {
				urlsCondition.awaitUninterruptibly();
			}
			// 根据全部url生成一组invoker
			ImmutableList.Builder<Invoker<T>> listBuilder = ImmutableList.builder();
			providerUrls.forEach(url -> {
				Invoker<T> invoker = protocal.refer(type, url);
				listBuilder.add(invoker);
			});
			invokersBeforeRoute = listBuilder.build();
		}finally {
			mutex.unlock();
		}
		// 利用所有router将invoker列表过滤一遍
		for(Router router : routers) {
			// 路由选择
			invokersAfterRoute = router.route(invokersBeforeRoute, localUrl, invocation);
			invokersBeforeRoute = invokersAfterRoute;
		}
		return invokersAfterRoute;
	}

	@Override
	public void notify(List<URL> urls) {
		Preconditions.checkState(isAvailable);
		Preconditions.checkNotNull(urls);
		// 将url列表更换一遍
		mutex.lock();
		try {
			// 清空url并重新载入url数据
			providerUrls.clear();
			providerUrls.addAll(urls);
			// 唤醒等待服务列表数据的线程
			urlsCondition.signalAll();
		}finally {
			mutex.unlock();
		}
	}

}
