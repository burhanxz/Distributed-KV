package rpc;

import java.util.concurrent.Future;

public interface Client {
	/**
	 * 
	 * @param invocation
	 * @return
	 * @throws Exception
	 */
	public Future<Result> request(Invocation invocation) throws Exception;
}
