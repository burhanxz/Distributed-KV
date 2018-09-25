package cluster;

/**
 * @author bird
 *
 */
public class ServerNode {
	/**
	 * 
	 */
	private String host;
	/**
	 * 
	 */
	private int port;
	/**
	 * @param host
	 * @param port
	 */
	protected ServerNode(String host, int port) {
		this.host = host;
		this.port = port;
	}
	/**
	 * @return
	 */
	public String getHost() {
		return host;
	}
	/**
	 * @return
	 */
	public int getPort() {
		return port;
	}	
}

