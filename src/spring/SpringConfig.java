package spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import provide.ComputeService;
import provide.ComputeServiceImpl;

/**
 * @author bird
 * spring配置类
 * 当需要引入新的服务时，需要在此处配置
 * 配置信息包括bean的名字，接口类和ServiceSubscribe配置
 */
@Configuration
public class SpringConfig {

	/**
	 * @return 返回ServiceSubscribe对象，代表了服务类及其配置
	 * 
	 */
	@Bean(name = "computeService")
	public ServiceSubscribe<ComputeService> getComputeService() {
		// 新建一个ServiceSubscribe对象，即FactoryBean对象
		ServiceSubscribe<ComputeService> subscribe = new ServiceSubscribe<ComputeService>();
		// 设置接口class
		subscribe.setInterfaceClazz(ComputeService.class);
		// 配置ServiceSubscribe
		subscribe.setConfig(new ServiceSubscribeConfig());

		return new ServiceSubscribe<ComputeService>();
	}
	@Bean(name = "computeService2")
	public ServicePublish getComputeService2() {
		ServicePublish ret = new ServicePublish();
		ret.setTimeout(1000);
		ret.setServiceImpl(new ComputeServiceImpl());
		ret.setServiceInterfaceClazz(ComputeService.class);
		return ret;
	}
}
