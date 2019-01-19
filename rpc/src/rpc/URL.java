package rpc;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

/**
 * URL格式是 host:port/path/parameters
 * @author bird
 *
 */
public class URL {
	private String host;
	private int port;
	private String path;
	private Map<String, String> parameters;
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder{
		private URL url = new URL();
		private StringBuilder sb = new StringBuilder();
		private Builder() {}
		public Builder host(String host) {
			url.host = host;
			return this;
		}
		public Builder port(int port) {
			url.port = port;
			return this;
		}
		public Builder appendPath(String path) {
			sb.append("/").append(path);
			return this;
		}
		public Builder appendParameter(String key, String value) {
			url.parameters.put(key, value);
			return this;
		}
		public URL build() {
			// 将string builder中的数据合成字符串返回
			url.path = sb.toString();
			return url;
		}
	} 
	/**
	 * 返回host:port格式的IP地址
	 * @return
	 */
	public String connectString() {
		StringBuilder sb = new StringBuilder();
		sb.append(host).append(':').append(port);
		return sb.toString();
	}
	public String getHost() {
		return host;
	}
	public int getPort() {
		return port;
	}
	public String getPath() {
		return path;
	}
	/**
	 * 获取参数表
	 * @return
	 */
	public Map<String, String> getParameters(){
		// 新建不可变map builder
		ImmutableMap.Builder<String, String> mapBuilder = ImmutableMap.builder();
		// 将parameter填充进map builder
		mapBuilder.putAll(parameters);
		// 建立不可变map并返回
		return mapBuilder.build();
	}
}
