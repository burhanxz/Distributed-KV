package test;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import provide.ComputeService;
import spring.SpringConfig;

/**
 * @author bird
 * 测试
 */
public class SpringTest {

	public static void main(String[] args) {
		ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
		//测试ServiceSubscribe，实现了FactoryBean
		ComputeService c = (ComputeService) ctx.getBean("computeService");
		System.out.println(c.compute(1,2));
		//测试ServicePublish，实现了IntializingBean
		Object o = ctx.getBean("computeService2");
		System.out.println(o.getClass());
		//返回结果：
		//class com.xuzhong.rpc.spring.ServicePublish
	}

}
