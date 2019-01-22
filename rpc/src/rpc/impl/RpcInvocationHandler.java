package rpc.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import rpc.Invocation;
import rpc.Invoker;
import rpc.Result;

public class RpcInvocationHandler implements InvocationHandler{
	private Invoker<?> invoker;
	public RpcInvocationHandler(Invoker<?> invoker) {
		this.invoker = invoker;
	}
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Invocation.Builder invocationBuilder = InvocationImpl.builder();
		invocationBuilder.methodName(method.getName());
		
		invocationBuilder.parameterTypes(method.getParameterTypes());
		invocationBuilder.args(args);
		// TODO 注入配置参数
		invocationBuilder.attachment(null, null);
		// 获取invocation对象
		Invocation invocation = invocationBuilder.build();
		// 执行调用
		Result result = invoker.invoke(invocation);
		// TODO 获取调用结果
		Object ret = null;
		return null;
	}

}
