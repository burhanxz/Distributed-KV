package spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import client.Subscribe;
import client.SubscribeConfig;
import provide.ComputeService;
import provide.ComputeServiceImpl;
import registry.Publish;

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
	public Subscribe<ComputeService> getComputeService() {
		// 新建一个ServiceSubscribe对象，即FactoryBean对象
		Subscribe<ComputeService> subscribe = new Subscribe<ComputeService>();
		// 设置接口class
		subscribe.setInterfaceClazz(ComputeService.class);
		// 配置ServiceSubscribe
		subscribe.setConfig(new SubscribeConfig());

		return new Subscribe<ComputeService>();
	}
	@Bean(name = "computeService2")
	public Publish getComputeService2() {
		Publish ret = new Publish();
		ret.setTimeout(1000);
		ret.setServiceImpl(new ComputeServiceImpl());
		ret.setServiceInterfaceClazz(ComputeService.class);
		return ret;
	}
}
