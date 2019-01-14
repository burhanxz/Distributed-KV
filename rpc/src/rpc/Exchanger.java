package rpc;

public interface Exchanger {
	/**
	 * 绑定获取服务端
	 * @param serverURL
	 * @return
	 */
	public Server bind(URL serverURL);
	/**
	 * 连接获取客户端
	 * @param clientURL
	 * @return
	 */
	public Client connect(URL clientURL);
}
