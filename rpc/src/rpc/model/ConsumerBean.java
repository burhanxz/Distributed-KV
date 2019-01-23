package rpc.model;

import org.springframework.beans.factory.InitializingBean;

public class ConsumerBean implements InitializingBean{
	private String interfaceClazz;
	private int timeout;
	private boolean retry;
	private boolean isAsync;
	private String loadBalance;
	private String clusterStrategy;
	/**
	 * 检查参数是否合法
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		// 不合法则抛出classNotFoundException
		Class.forName(interfaceClazz);
		Class.forName(loadBalance);
		Class.forName(clusterStrategy);
	}
	public String getInterfaceClazz() {
		return interfaceClazz;
	}
	public void setInterfaceClazz(String interfaceClazz) {
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
	public String getLoadBalance() {
		return loadBalance;
	}
	public void setLoadBalance(String loadBalance) {
		this.loadBalance = loadBalance;
	}
	public String getClusterStrategy() {
		return clusterStrategy;
	}
	public void setClusterStrategy(String clusterStrategy) {
		this.clusterStrategy = clusterStrategy;
	}

}
