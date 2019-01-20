package rpc.impl;

import rpc.Client;
import rpc.Exchanger;
import rpc.Exporter;
import rpc.Invoker;
import rpc.Protocol;
import rpc.URL;

public class ProtocolImpl implements Protocol{
	private Exchanger exchanger;
	@Override
	public <T> Exporter<T> export(Invoker<T> invoker) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> Invoker<T> refer(Class<T> type, URL url) {
		// 准备client供invoker传输数据
		Client client = exchanger.connect(url);
		// 新建invoker
		Invoker<T> invoker = new ClientInvoker<>(type, url, client);
		return invoker;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
