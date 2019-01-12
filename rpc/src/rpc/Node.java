package rpc;

public interface Node {
	/**
	 * 获取本节点的url
	 * @return
	 */
	public URL getUrl();
	/**
	 * 判断本节点是否可用
	 * @return
	 */
	public boolean isAvailable();
	/**
	 * 销毁节点
	 */
	public void destroy();
}
