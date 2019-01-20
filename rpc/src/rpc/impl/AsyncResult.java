package rpc.impl;

import rpc.Result;
import rpc.ResultFuture;

public class AsyncResult implements Result{
	private final ResultFuture future;
	public AsyncResult(ResultFuture future) {
		this.future = future;
	}
	@Override
	public boolean hasException() {
		return future.getResult().hasException();
	}

	@Override
	public Throwable getException() {
		return future.getResult().getException();
	}

	@Override
	public Object getValue() {
		return future.getResult().getValue();
	}

}
