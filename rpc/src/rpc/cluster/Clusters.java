package rpc.cluster;

public class Clusters {
	/**
	 * to-do
	 * 
	 * @return
	 */
	public static Cluster newCluster(Strategy strategy) {
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
