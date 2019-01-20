package rpc.impl;

import java.util.List;

import rpc.Invocation;
import rpc.Invoker;
import rpc.Router;
import rpc.URL;

public class RouterImpl implements Router{

	@Override
	public int compareTo(Router o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public URL getUrl() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		// TODO Auto-generated method stub
		return null;
	}

}
