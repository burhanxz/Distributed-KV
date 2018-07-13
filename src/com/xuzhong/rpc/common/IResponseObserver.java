package com.xuzhong.rpc.common;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;

import com.xuzhong.rpc.common.data.IResponse;

public class IResponseObserver implements Observer {

	private InetSocketAddress address;

	private Map<Long, IResponse> responses = new ConcurrentHashMap<>();

	public IResponseObserver(InetSocketAddress address) {
		this.address = address;
	}

	@Override
	public void update(Observable iResponseObservable, Object args) {
	
		Object[] parameters = (Object[]) args;

		System.out.println(this.address);
		if (((InetSocketAddress) parameters[1]).equals(this.address)) {
			IResponse iResponse = (IResponse) parameters[0];
			responses.putIfAbsent(iResponse.getId(), iResponse);
		}

	}

	public IResponse getResponse(Long id) {
		if (responses.containsKey(id)) {
			IResponse iResponse = responses.get(id);
			responses.remove(id);
			return iResponse;
		}

		else
			return null;
	}

}
