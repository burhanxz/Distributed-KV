package rpc.impl;

import java.net.Inet4Address;
import java.util.Map;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import rpc.Cluster;

import rpc.Invoker;
import rpc.LoadBalance;
import rpc.ProxyFactory;
import rpc.Registry;
import rpc.URL;
import rpc.model.ApplicationBean;

import rpc.model.ProtocolBean;
import rpc.model.ProviderConstants;
import rpc.model.RegistryBean;
import rpc.model.RequestConstants;

/**
 * consumer bean 的工厂bean，利用spring IOC产生rpc代理对象
 * @author bird
 *
 * @param <T>
 */
public class ConsumerConfig<T> implements FactoryBean<T>, InitializingBean, DisposableBean{
	/**
	 * 集群
	 */
	private Cluster cluster;
	/**
	 * 注册中心
	 */
	private Registry registry;
	/**
	 * 代理工厂
	 */
	private ProxyFactory proxyFactory;
	/**
	 * bean生命状态
	 */
	private volatile boolean destroyed;
	/**
	 * 最终获取的bean，即代理对象
	 */
	private T ref;
    /**
     * 本地url
     */
    private URL localUrl;
    /**
     * 注册中心url
     */
    private URL registryUrl;
    /*以下是基础全局配置*/
	/**
	 * appkey配置
	 */
	private ApplicationBean applicationBean;
	/**
	 * 注册中心配置
	 */
	private RegistryBean registryBean;	
    /**
     * 端口配置
     */
    private ProtocolBean protocolBean;
    /*以下是consumerBean配置*/
	/**
	 * 服务接口
	 */
	private Class<T> interfaceClazz;
	/**
	 * 服务调用超时时间
	 */
	private int timeout;
	/**
	 * 是否失败重试
	 */
	private boolean retry;
	/**
	 * 服务调用是否异步
	 */
	private boolean isAsync;
	/**
	 * 负载均衡策略
	 */
	private Class<LoadBalance> loadBalanceClazz;
	/**
	 * 集群策略
	 */
	private Class<Cluster> clusterClazz;
	public ConsumerConfig() {
	}
	/*
	 * 属性设置完成后，根据属性初始化RPC组件：Registry, Cluster, ProxyFactory
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO 组装本地url即consumer url
		localUrl = URL.builder().host(Inet4Address.getLocalHost().getHostAddress()).port(protocolBean.getPort()).build();
		// 获取registry url
		registryUrl = URL.builder().host(registryBean.getHost()).port(registryBean.getPort()).build();
		// 设置destroy状态
		destroyed = false;
		// 初始化cluster
		cluster = clusterClazz.newInstance();
		// 初始化register
		registry = ZookeeperRegistryFactory.getInstance().getRegistry(registryUrl);
		if(registry == null) {
			throw new Exception("注册中心不可用");
		}
		// 初始化proxyFactory
		proxyFactory = JdkProxyFactory.getInstance();
	}
	@Override
	public T getObject() throws Exception {
		// 检查是否被销毁
		Preconditions.checkState(!destroyed);
		// 如果ref不存在，初始化ref
		if(ref == null) {
			ref = getProxy(interfaceClazz);
		}
		return ref;
	}
	@Override
	public Class<T> getObjectType() {
		return interfaceClazz;
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
	
	private T getProxy(Class<T> clazz) throws Exception{
		// 建立注册中心url，传入类型信息
		URL subscribeUrl = URL.builder().host(localUrl.getHost()).port(localUrl.getPort())
				.appendPath(applicationBean.getAppKey())
				.appendPath(interfaceClazz.getSimpleName())
				.appendPath(ProviderConstants.PROVIDERS).build();
		// 创建directory
		NotifiedDirectroy<T> directory = new NotifiedDirectroy<>(interfaceClazz, localUrl);
		// 将directory作为监听器，订阅注册中心消息
		registry.subscribe(subscribeUrl, directory);
		// 将directory作为invoker集合，通过cluster合并成一个虚拟invoker
		Invoker<T> invoker = cluster.join(directory);
		// 组装options信息
		ImmutableMap.Builder<String, String> optionsBuilder = ImmutableMap.builder();
		Map<String, String> options = optionsBuilder
			.put(RequestConstants.IS_ASYNC, String.valueOf(isAsync))
			.put(RequestConstants.TIMEOUT, String.valueOf(timeout))
			.put(RequestConstants.LOAD_BALANCE, loadBalanceClazz.getName())
			.put(RequestConstants.RETRY, String.valueOf(retry))
			.build();
		// 通过invoker获取proxy
		T proxy = proxyFactory.getProxy(invoker, options);
		return proxy;
	}
	public ApplicationBean getApplicationBean() {
		return applicationBean;
	}
	public void setApplicationBean(ApplicationBean applicationBean) {
		this.applicationBean = applicationBean;
	}
	public RegistryBean getRegistryBean() {
		return registryBean;
	}
	public void setRegistryBean(RegistryBean registryBean) {
		this.registryBean = registryBean;
	}
	public ProtocolBean getProtocolBean() {
		return protocolBean;
	}
	public void setProtocolBean(ProtocolBean protocolBean) {
		this.protocolBean = protocolBean;
	}
	public Class<T> getInterfaceClazz() {
		return interfaceClazz;
	}
	public void setInterfaceClazz(Class<T> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}
	public int getTimeout() {
		return timeout;
	}
	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	public boolean isRetry() {
		return retry;
	}
	public void setRetry(boolean retry) {
		this.retry = retry;
	}
	public boolean isAsync() {
		return isAsync;
	}
	public void setAsync(boolean isAsync) {
		this.isAsync = isAsync;
	}
	public Class<LoadBalance> getLoadBalanceClazz() {
		return loadBalanceClazz;
	}
	public void setLoadBalanceClazz(Class<LoadBalance> loadBalanceClazz) {
		this.loadBalanceClazz = loadBalanceClazz;
	}
	public Class<Cluster> getClusterClazz() {
		return clusterClazz;
	}
	public void setClusterClazz(Class<Cluster> clusterClazz) {
		this.clusterClazz = clusterClazz;
	}



}
