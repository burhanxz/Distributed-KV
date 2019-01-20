package rpc;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;

/**
 * URL格式是 host:port/path/parameters
 * @author bird
 *
 */
public class URL {
	private ProtocolType protocol;
	private String host;
	private int port;
	private String path;
	private Map<String, String> parameters;
	public static Builder builder() {
		return new Builder();
	}
	public static class Builder{
		private URL url = new URL();
		private StringBuilder pathSb = new StringBuilder();
		private Builder() {}
		public Builder protocol(ProtocolType protocol) {
			url.protocol = protocol;
			return this;
		}
		public Builder host(String host) {
			url.host = host;
			return this;
		}
		public Builder port(int port) {
			url.port = port;
			return this;
		}
		public Builder str(String url) {
			// TODO
			return this;
		}
		public Builder bytes(byte[] bytes) {
			// 生成字符串并分割
			String connectStr = new String(bytes);
			String[] strs = connectStr.split(":");
			Preconditions.checkArgument(strs.length == 2);
			// 设置host和port
			url.host = strs[0];
			url.port = Integer.valueOf(strs[1]);
			return this;
		}
		public Builder appendPath(String path) {
			pathSb.append("/").append(path);
			return this;
		}
		public Builder appendParameter(String key, String value) {
			url.parameters.put(key, value);
			return this;
		}
		public URL build() {
			// 将string builder中的数据合成字符串返回
			url.path = pathSb.toString();
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
	public ProtocolType getProtocol() {
		return protocol;
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
	
	/**
	 * 协议类型:消费者，生产者和注册中心
	 * @author bird
	 *
	 */
	public enum ProtocolType{
		Consumer("consumer", 1), Provider("provider", 2), Registry("registry", 0);
		/**
		 * 类型字符串
		 */
		private String typeName;
		/**
		 * 枚举类标识
		 */
		private int id;
		private ProtocolType(String typeName, int id) {
			this.typeName = typeName;
			this.id = id;
		}
		public String getTypeName() {
			return typeName;
		}
		public int getId() {
			return id;
		}
	}
}
