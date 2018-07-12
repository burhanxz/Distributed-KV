package com.xuzhong.rpctest.test.clientFactory;

public interface Client {
	/*heart beat check*/
	public void startHeartBeat() throws Exception;
	/*send a request*/
	public void sendRequest(Object msg) throws Exception;
	
	public String getDomain();
	
	public boolean isConnected();
}
