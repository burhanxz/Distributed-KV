package rpc.impl;

import rpc.Exporter;
import rpc.Invoker;
import rpc.Protocol;
import rpc.URL;

public class ProtocalImpl implements Protocol{

	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
