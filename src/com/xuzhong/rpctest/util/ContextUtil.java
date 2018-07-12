package com.xuzhong.rpctest.util;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.xuzhong.rpctest.conf.ClientConfig;

/**
 * @author bird
 * 
 * 
 *
 */
public class ContextUtil {
	private static final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
	
	static {
		context.register(ClientConfig.class);
		context.refresh();
	}
	
	public static <T> T getBean(Class<T> beanClass) {
		
		T bean = context.getBean(beanClass);		
		
		return bean;
	}
	
}
