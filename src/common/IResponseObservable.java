package common;

import java.net.InetSocketAddress;
import java.util.Observable;

import data.IResponse;
//a IResponseObservable is related with several channels
public class IResponseObservable extends Observable{
	
	public void pushResponse(IResponse response, InetSocketAddress address) {
		setChanged();
		notifyObservers(new Object[] {response, address});
	}
	
}
