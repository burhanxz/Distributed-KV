package com.xuzhong.rpc.test.clientFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.xuzhong.rpc.test.clientFactory.pojo.RemotingUrl;

public abstract class AbstractClientFactory implements ClientFactory{
	private final Cache<RemotingUrl, Client> cacherClients = CacheBuilder.newBuilder()
			.maximumSize(65535)
			.expireAfterAccess(27, TimeUnit.MINUTES)
			.removalListener(new RemovalListener<RemotingUrl, Client>(){

				@Override
				public void onRemoval(RemovalNotification<RemotingUrl, Client> arg0) {
					
					//close link
				}
				
				
			}).build();
	
	protected abstract Client createClient(RemotingUrl domain) throws Exception;
	
	@Override
	public Client get(RemotingUrl url) throws Exception {
		 Client client = cacherClients.get(url, new Callable<Client>() {

	            @Override
	            public Client call() throws Exception {
	                Client client = createClient(url);
	                if (null != client) {
	                	
	                	/*when get a client, we should firstly make heart beat to ensure link*/
	                	/*if link is break down then we will re-link*/
	                    client.startHeartBeat();
	                }
	                return client;
	            }

	        });
	        return client;
	}

	@Override
	public List<Client> getAllClients() throws Exception {
        List<Client> result = new ArrayList<Client>((int) cacherClients.size());
        result.addAll(cacherClients.asMap().values());
        return result;
	}

	@Override
	public void remove(RemotingUrl url) throws Exception {
		 cacherClients.invalidate(url);
		
	}

}
