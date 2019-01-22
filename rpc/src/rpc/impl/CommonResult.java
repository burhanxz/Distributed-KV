package rpc.impl;

import rpc.Result;


/**
 * 普通result实现
 * @author bird
 *
 */
public class CommonResult implements Result{
	/**
	 * 是否有exception
	 */
	private boolean hasException;
	/**
	 * 存放的异常对象
	 */
	private Throwable exception;
	/**
	 * 普通对象
	 */
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
