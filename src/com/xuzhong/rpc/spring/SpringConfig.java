package com.xuzhong.rpc.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.xuzhong.rpc.service.ComputeService;

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
}
