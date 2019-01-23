package rpc.impl;

import java.util.Map;

import com.google.common.collect.ImmutableMap;

import rpc.Invocation;

public class InvocationImpl implements Invocation{
	private String methodName;
	private Class<?>[] parameterTypes;
	private Object[] args;
	private ImmutableMap<String, String> options;
	
	private InvocationImpl() {}
	
	public static Builder builder() {
		return new BuilderImpl();
	}
	
	public static class BuilderImpl implements Invocation.Builder{
		private InvocationImpl invocation = new InvocationImpl();
		private ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		private BuilderImpl() {}
		@Override
		public Builder methodName(String methodName) {
			invocation.methodName = methodName;
			return this;
		}
		@Override
		public Builder parameterTypes(Class<?>[] parameterTypes) {
			invocation.parameterTypes = parameterTypes;
			return this;
		}
		@Override
		public Builder args(Object[] args) {
			invocation.args = args;
			return this;
		}
		@Override
		public Builder option(String key, String value) {
			builder.put(key, value);
			return this;
		}
		@Override
		public Builder options(Map<String, String> options) {
			builder.putAll(options);
			return null;
		}
		@Override
		public Invocation build() {
			invocation.options = builder.build();
			return invocation;
		}
	}
	
	@Override
	public String getMethodName() {
		return methodName;
	}

	@Override
	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public Object[] getArgs() {
		return args;
	}

	@Override
	public Map<String, String> getOptions() {
		return options;
	}

}
