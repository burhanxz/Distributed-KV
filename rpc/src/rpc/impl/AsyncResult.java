package rpc.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rpc.Result;

/**
 * 异步结果
 * @author bird
 *
 */
public class AsyncResult implements Result{
	/**
	 * future存放异步结果
	 */
	private final Future<Result> future;
	/**
	 * 实际存放数据的result
	 */
	private volatile Result result;
	/**
	 * 超时时间
	 */
	private final int timeout;
	public AsyncResult(Future<Result> future, int timeout) {
		this.future = future;
		this.result = null;
		this.timeout = timeout;
	}
	@Override
	public boolean hasException() {
		return getResult().hasException();
	}
	@Override
	public Throwable getException() {
		return getResult().getException();
	}
	@Override
	public Object getValue() {
		return getResult().getValue();
	}
	/**
	 * 获取异步数据
	 * @return
	 */
	private Result getResult() {
		if(result == null) {
			try {
				// 在有限时间内获取异步结果
				result = future.get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
