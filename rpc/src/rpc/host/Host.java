package rpc.host;

/**
 * @author bird
 *
 */
public class Host {
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
	public Host(String host, int port) {
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

	@Override
	public String toString() {
		return host + ":" + port;
	}
}
