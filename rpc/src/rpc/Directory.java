package rpc;

import java.util.List;

public interface Directory<T> extends Node{
	/**
	 * 获取invoker列表所属接口
	 * @return
	 */
	public Class<T> getInterface();
	/**
	 * 根据invocation列举所有可用的invoker
	 * @param invocation
	 * @return
	 */
	public List<Invoker<T>> list(Invocation invocation);
}
