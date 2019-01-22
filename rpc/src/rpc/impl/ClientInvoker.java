package rpc.impl;

import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;

import rpc.Client;
import rpc.Invocation;
import rpc.Invoker;
import rpc.Result;
import rpc.URL;
import rpc.model.RequestConstants;

/**
 * 实际执行调用的invoker
 * @author bird
 *
 * @param <T> 接口类型
 */
public class ClientInvoker<T> implements Invoker<T>{
	/**
	 * 接口类型
	 */
	private Class<T> type;
	private URL remoteUrl;
	/**
	 * 客户端传输
	 */
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
	public Result invoke(Invocation invocation) throws Exception {
		// 获取invocation配置信息
		Map<String, String> attachments = invocation.getAttachments();
		// 获取超时时间
		String timeoutStr = attachments.get(RequestConstants.TIMEOUT);
		int timeout = timeoutStr == null ? RequestConstants.TIMEOUT_DEFAULT : Integer.parseInt(timeoutStr);
		// 判断是否异步
		String isAsynStr = attachments.get(RequestConstants.IS_ASYNC);
		// 判断是否设置为异步
		boolean isAsyn = false;
		if(isAsynStr != null && Boolean.parseBoolean(isAsynStr)) {
			isAsyn = true;
		}
		// 实际网络传输
		Future<Result> future = client.request(invocation);
		// 返回异步result
		if(isAsyn) {
			return new AsyncResult(future, timeout);
		}
		// 阻塞同步
		Result result = future.get(timeout, TimeUnit.MILLISECONDS);
		return result;
		
	}


}
