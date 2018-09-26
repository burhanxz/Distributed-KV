package cluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.SortedMap;

public class IPHashCluster extends HashCluster {

	@Override
	public String getNode(Class<?> serviceInterface) {
		addNodes(serviceInterface);
		
		try {
			InetAddress ip = InetAddress.getLocalHost();

			String host = ip.getHostAddress();

			int hashCode = hash(host) % HASH_SIZE;

			ServerNode node = null;
			if (hashCode > virtualNodes.lastKey()) {

				node = virtualNodes.get(virtualNodes.firstKey());
			} else {
				SortedMap<Integer, ServerNode> subMap = virtualNodes.tailMap(hashCode);
				node = subMap.get(subMap.firstKey());
			}

		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

}
