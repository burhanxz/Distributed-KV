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
	public String getNode();

	/**
	 * to-do
	 * 
	 * @return
	 */
	default public Cluster newCluster(Strategy strategy) {
		switch (strategy) {
		case IPHash:
		case Polling:
		default:

		}
		return null;
	}

	/**
	 * @author bird
	 * 代表负载均衡策略的枚举类
	 */
	public enum Strategy {
		IPHash, Polling
	}
}
