package rpc.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import com.google.common.base.Preconditions;

import rpc.Invocation;
import rpc.Invoker;
import rpc.Result;

/**
 * 实现动态代理的核心执行过程
 * @author bird
 *
 */
public class RpcInvocationHandler implements InvocationHandler{
	/**
	 * 实际执行体
	 */
	private Invoker<?> invoker;
	/**
	 * invocation设置
	 */
	private Map<String, String> options;
	public RpcInvocationHandler(Invoker<?> invoker, Map<String, String> options) {
		this.invoker = invoker;
		this.options = options;
		Preconditions.checkArgument(invoker instanceof ClusterInvoker);
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// 组装invocation
		Invocation.Builder invocationBuilder = InvocationImpl.builder();
		// 组装动态调用信息：方法，参数类型和参数
		invocationBuilder.methodName(method.getName());
		invocationBuilder.parameterTypes(method.getParameterTypes());
		invocationBuilder.args(args);
		// 注入配置参数
		invocationBuilder.options(options);
		// 获取invocation对象
		Invocation invocation = invocationBuilder.build();
		// 执行调用
		Result result = invoker.invoke(invocation);
		return result;
	}

}
