package registry;


import org.springframework.beans.factory.InitializingBean;

/**
 * @author bird
 *
 */
public class Publish implements InitializingBean{

	private Object serviceImpl;
	
	private Class<?> serviceInterfaceClazz;
	
	private long timeout;
	
	private int threads;
	
	/*核心方法，在bean参数设置完成之后进行zookeeper服务注册*/
	@Override
	public void afterPropertiesSet() throws Exception {
		
		
	}

	public Object getServiceImpl() {
		return serviceImpl;
	}

	public void setServiceImpl(Object serviceImpl) {
		this.serviceImpl = serviceImpl;
	}

	public Class<?> getServiceInterfaceClazz() {
		return serviceInterfaceClazz;
	}

	public void setServiceInterfaceClazz(Class<?> serviceInterfaceClazz) {
		this.serviceInterfaceClazz = serviceInterfaceClazz;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}




}
