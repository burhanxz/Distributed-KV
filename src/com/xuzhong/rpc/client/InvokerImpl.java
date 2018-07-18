package com.xuzhong.rpc.client;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.Observer;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.xuzhong.rpc.common.IResponseObservable;
import com.xuzhong.rpc.common.IResponseObserver;
import com.xuzhong.rpc.common.data.IRequest;
import com.xuzhong.rpc.common.data.IResponse;
import com.xuzhong.rpc.facet.Log;
import com.xuzhong.rpc.util.ProtostuffUtil;
import com.xuzhong.rpc.util.SerialNumberUtil;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;

/**
 * implement invoke interface
 * 
 * @author bird
 *
 */
public class InvokerImpl implements Invoker {

	/*
	 * a invoker composes of a channel(represent a server) , and a observer(bind to
	 * a specific server)
	 */
	private static final ExecutorService monitorPool = Executors.newFixedThreadPool(128);
	
	private Channel channel;

	private InetSocketAddress address;

	private IResponseObserver observer;

	/*
	 * register observer to observable; bind channel and address
	 */
	protected InvokerImpl(Channel channel, InetSocketAddress address, IResponseObservable observable) {
		this.channel = channel;
		this.address = address;
		observer = new IResponseObserver(this.address);
		observable.addObserver(observer);
	}

	/*
	 * invoke a remote method
	 * 
	 * @see com.xuzhong.rpctest.Invoker#invoke(java.lang.Class,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */

	@Override
	public Object invoke(Class<?> interfaceClazz, Method method, Object[] parameters) {

		long id = SerialNumberUtil.makeSerialNumber();

		IRequest iRequest = new IRequest(id, interfaceClazz, method, parameters);
		IResponse iResponse = null;

		byte[] msg = ProtostuffUtil.serializer(iRequest);

		/*
		 * key process. write message to channel, which means to transfer message via
		 * network
		 */
		Log.logger.info(channel.localAddress().toString() + ":" + iRequest);
		synchronized (this) {
			channel.writeAndFlush(Unpooled.copiedBuffer(msg));
		}

		/* monitor responses */
		Future<IResponse> future = monitorPool.submit(new Callable<IResponse>() {

			/*
			 * Continually inquiry the observer about response by a infinite loop
			 */
			@Override
			public IResponse call() throws Exception {
				IResponse iResponse = null;
				while (true) {
					iResponse = observer.getResponse(id);
					if (iResponse == null)
						continue;
					return iResponse;
				}

			}

		});
		/* get response from thread */
		try {
			iResponse = future.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		// future.closeFuture().sync();
		return (Object) iResponse.getReturnValue();
	}

	/*
	 * make heart beat with server, if link is break down then client should re-link
	 */
	@Override
	public void startHeartBeat() throws Exception {
		throw new UnsupportedOperationException();

	}

	@Override
	public String getDomain() {
		return ((InetSocketAddress) channel.localAddress()).getHostString();
	}

	@Override
	public boolean isConnected() {

		return channel.isActive();
	}
	
	@Override
	public boolean terminateChannel() {
		try {

			this.channel.closeFuture().sync();

		} catch (Throwable t) {
			return false;
		}

		return true;
	}

}
