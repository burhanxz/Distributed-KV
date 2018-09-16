package zookeeper;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

/**
 * 注册信息，序列化为json后存放在ZooKeeper服务器上
 * @author bird
 *
 */
public class RegisterInfo {
	private Class<?> interfaceClazz;
	private String application;
	private String host;
	private int port;
	private float weight;
	private long timeout;
	
	public String toString() {
		return JSON.toJSONString(this, SerializerFeature.WriteMapNullValue);
	}
	
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
	
	
}
