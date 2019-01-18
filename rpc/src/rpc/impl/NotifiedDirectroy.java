package rpc.impl;

import java.util.List;

import rpc.Directory;
import rpc.Invocation;
import rpc.Invoker;
import rpc.NotifyListener;
import rpc.URL;

public class NotifiedDirectroy<T> implements NotifyListener, Directory<T>{

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

	@Override
	public Class<T> getInterface() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Invoker<T>> list(Invocation invocation) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notify(List<URL> urls) {
		// TODO Auto-generated method stub
		
	}

}
