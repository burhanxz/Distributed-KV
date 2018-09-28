package registry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 注册信息，序列化为json后存放在ZooKeeper服务器上
 * @author bird
 *
 */
public class RegisterInfo {
	/**
	 * 接口类
	 */
	private Class<?> interfaceClazz;
	/**
	 * 应用标识
	 */
	private String application;
	/**
	 * 对外提供服务的主机号
	 */
	private String host;
	/**
	 * 对外提供服务的端口号
	 */
	private int port;
	/**
	 * 权重，供负载均衡时参考
	 */
	private float weight;
	/**
	 * 超时失效时间，单位：ms
	 */
	private long timeout;
	/**
	 * 实现类对象
	 */
	private Object implementClazz;
	
	/* 
	 * 序列化本对象，包括类名信息
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteClassName);
	}
	
	//以下是getter和setter
	public Class<?> getInterfaceClazz() {
		return interfaceClazz;
	}
	public void setInterfaceClazz(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}
	public String getApplication() {
		return application;
	}
	public void setApplication(String application) {
		this.application = application;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public Object getImplementClazz() {
		return implementClazz;
	}

	public void setImplementClazz(Object implementClazz) {
		this.implementClazz = implementClazz;
	}
	
	
}
