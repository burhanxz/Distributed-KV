package client;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

public interface InvokerFactory {

	public Invoker get(InetSocketAddress address) throws Exception;
	
	public List<Invoker> listAll();
	
	public boolean remove(InetSocketAddress address);
	
	public boolean removeAll();

	public boolean checkConnect(InetSocketAddress address);
	
	public Map<InetSocketAddress, Boolean> checkAllConnect();
	
}
