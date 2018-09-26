package cluster;

import java.util.List;

/**
 * @author bird
 *
 */
public interface Cluster {
	/**
	 * @param 
	 * @return
	 */
	public String getNode(Class<?> serviceInterface);

	/**
	 * to-do
	 * 
	 * @return
	 */
	default public Cluster newCluster(Strategy strategy) {
		Cluster cluster = null;
		switch (strategy) {
		case IPHash:
			cluster = new IPHashCluster();
			break;
		case Polling:
		default:
			cluster = new IPHashCluster();
		}
		return cluster;
	}

	/**
	 * @author bird
	 * 代表负载均衡策略的枚举类
	 */
	public enum Strategy {
		IPHash, Polling
	}

}
