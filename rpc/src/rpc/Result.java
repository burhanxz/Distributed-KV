package rpc;

public interface Result {
	/**
	 * 判断返回的结果中是否有异常
	 * @return
	 */
	public boolean hasException();
	/**
	 * 获取异常
	 * @return
	 */
	public Throwable getException();
	/**
	 * 获取正常返回结果
	 * @return
	 */
	public Object getValue();
}
