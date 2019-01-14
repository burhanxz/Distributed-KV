package rpc.cluster;

public class Clusters {
	/**
	 * to-do
	 * 
	 * @return
	 */
	public static Cluster newCluster(Strategy strategy) {
		// TODO
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
