package cluster;

import core.Pipeline;

/**
 * @author bird
 *
 */
public interface Cluster extends Pipeline{
	/**
	 * @param 
	 * @return
	 */
	public String getHost(Class<?> serviceInterface);
	
	

}
