package rpc;

public interface Client {
	public ResultFuture request(Invocation invocation) throws Exception;
}
