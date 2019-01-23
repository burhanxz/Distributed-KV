package rpc;

import java.util.Map;

import rpc.Invocation.Builder;

/**
 * 存放一次方法调用所需的信息
 * @author bird
 *
 */
public interface Invocation {
	/**
	 * 获取方法名
	 * @return
	 */
	public String getMethodName();
	/**
	 * 获取各个方法参数的类型
	 * @return
	 */
	public Class<?>[] getParameterTypes();
	/**
	 * 获取方法中各个参数
	 * @return
	 */
	public Object[] getArgs();
	/**
	 * 返回invocation配置
	 * @return
	 */
	public Map<String, String> getOptions();
	
	public interface Builder{
		public Builder methodName(String methodName);
		public Builder parameterTypes(Class<?>[] parameterTypes);
		public Builder args(Object[] args);
		public Builder option(String key, String value);
		public Builder options(Map<String, String> options);
		public Invocation build();
	}
}
