package rpc.impl;

import rpc.Result;
import rpc.ResultFuture;

public class CommonResult implements Result{
	private boolean hasException;
	private Throwable exception;
	private Object value;
	
	public CommonResult(boolean hasException, Throwable exception, Object value) {
		this.hasException = hasException;
		this.exception = exception;
		this.value = value;
	}

	@Override
	public boolean hasException() {
		return hasException;
	}

	@Override
	public Throwable getException() {
		return exception;
	}

	@Override
	public Object getValue() {
		return value;
	}

}
