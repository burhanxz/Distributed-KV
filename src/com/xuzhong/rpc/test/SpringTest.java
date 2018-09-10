package com.xuzhong.rpc.test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.xuzhong.rpc.service.ComputeService;
import com.xuzhong.rpc.spring.SpringConfig;

/**
 * @author bird
 * 测试
 */
public class SpringTest {

	public static void main(String[] args) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		ComputeService c = (ComputeService) ctx.getBean("computeService");
		System.out.println(c.compute(1,2));
	}

}
