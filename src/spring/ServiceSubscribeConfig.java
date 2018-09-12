package spring;

/**
 * @author bird
 * serviceSubscribe服务订阅的配置类
 * 待增加
 */
public class ServiceSubscribeConfig {
	/*连接超时时间 单位：ms*/
	private long timeout = 1000;

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	
	
}
