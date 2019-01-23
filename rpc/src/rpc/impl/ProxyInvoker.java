package rpc.impl;

import rpc.Invocation;
import rpc.Invoker;
import rpc.Result;
import rpc.URL;

/**provider方所用invoker
 * @author bird
 *
 * @param <T>
 */
public class ProxyInvoker<T> implements Invoker<T>{

	@Override
	public Class<T> getInterface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Result invoke(Invocation invocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
