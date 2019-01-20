package rpc.impl;

import java.util.Map;

import com.google.common.base.Preconditions;

import rpc.Client;
import rpc.Invocation;
import rpc.Invoker;
import rpc.Result;
import rpc.ResultFuture;
import rpc.URL;
import rpc.model.RequestConstants;

public class ClientInvoker<T> implements Invoker<T>{
	private Class<T> type;
	private URL remoteUrl;
	private Client client;
	public ClientInvoker(Class<T> type, URL remoteUrl, Client client) {
		Preconditions.checkArgument(remoteUrl.getProtocol() == URL.ProtocolType.Provider);
		this.type = type;
		this.remoteUrl = remoteUrl;
		this.client = client;
	}
	@Override
	public Class<T> getInterface() {
		return type;
	}

	@Override
	public Result invoke(Invocation invocation) {
		// 获取invocation配置信息
		Map<String, String> attachments = invocation.getAttachments();
		String isAsynStr = attachments.get(RequestConstants.IS_ASYNC);
		// 判断是否设置为异步
		boolean isAsyn = false;
		if(isAsynStr != null && Boolean.parseBoolean(isAsynStr)) {
			isAsyn = true;
		}
		ResultFuture future = client.request(invocation);
		// 返回异步result
		if(isAsyn) {
			return new AsyncResult(future);
		}
		// 阻塞同步
		Result result = future.getResult();
		return result;
		
	}


}
