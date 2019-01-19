package rpc.impl;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Preconditions;

import rpc.Cluster;

import rpc.Invoker;
import rpc.ProxyFactory;
import rpc.Registry;
import rpc.URL;

public class ConsumerConfig<T> implements FactoryBean<T>, InitializingBean, DisposableBean{
	private Cluster cluster;
	private Registry registry;
	private ProxyFactory proxyFactory;
	private volatile boolean destroyed;
	private T ref;
    /**
     * 服务接口
     */
    private Class<T> targetInterface;
    /**
     * 注册中心url
     */
    private URL registryUrl;
    /**
     * 集群策略
     */
    private Class<Cluster> clusterStrategy;
    /**
     * 服务提供者唯一标识
     */
    private String remoteAppKey;
    /**
     * 服务分组组名
     */
    private String groupName;
    
	public ConsumerConfig() {
	}
	/*
	 * 属性设置完成后，根据属性初始化RPC组件：Registry, Cluster, ProxyFactory
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// 初始化cluster
		cluster = clusterStrategy.newInstance();
		// 初始化register
		registry = ZookeeperRegistryFactory.getInstance().getRegistry(registryUrl);
		// 初始化proxyFactory
		proxyFactory = JdkProxyFactory.getInstance();
	}
	@Override
	public T getObject() throws Exception {
		// 检查是否被销毁
		Preconditions.checkState(!destroyed);
		// 如果ref不存在，初始化ref
		if(ref == null) {
			ref = getProxy(targetInterface);
		}
		return ref;
	}
	@Override
	public Class<T> getObjectType() {
		return targetInterface;
	}
	@Override
	public boolean isSingleton() {
		// 代理对象是单例类
		return true;
	}
	
	@Override
	public void destroy() throws Exception {
		// TODO Auto-generated method stub
	}
	
	private T getProxy(Class<T> clazz){
		// 建立注册中心url，传入类型信息
		URL subscribeUrl = null;
		// 创建directory
		NotifiedDirectroy<T> directory = new NotifiedDirectroy<T>();
		// 将directory作为监听器，订阅注册中心消息
		registry.subscribe(subscribeUrl, directory);
		// 将directory作为invoker集合，通过cluster合并成一个虚拟invoker
		Invoker<T> invoker = cluster.join(directory);
		// 通过invoker获取proxy
		T proxy = proxyFactory.getProxy(invoker);
		return proxy;
	}

	public URL getRegistryUrl() {
		return registryUrl;
	}

	public void setRegistryUrl(URL registryUrl) {
		this.registryUrl = registryUrl;
	}

	public Class<Cluster> getClusterStrategy() {
		return clusterStrategy;
	}

	public void setClusterStrategy(Class<Cluster> clusterStrategy) {
		this.clusterStrategy = clusterStrategy;
	}

	public String getRemoteAppKey() {
		return remoteAppKey;
	}

	public void setRemoteAppKey(String remoteAppKey) {
		this.remoteAppKey = remoteAppKey;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Class<T> getTargetInterface() {
		return targetInterface;
	}

	public void setTargetInterface(Class<T> targetInterface) {
		this.targetInterface = targetInterface;
	}




}
