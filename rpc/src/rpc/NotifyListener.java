package rpc;

import java.util.List;

public interface NotifyListener {
	/**
	 * 当接收到服务改变消息时，调用此方法
	 * @param urls 同lookup方法获取到的数据
	 */
	public void notify(List<URL> urls);
}
