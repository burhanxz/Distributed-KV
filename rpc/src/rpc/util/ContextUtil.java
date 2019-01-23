package rpc.util;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author bird
 * 
 * 
 *
 */
public class ContextUtil {
	private static final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
	
	static {
		// TODO
		context.register(null);
		context.refresh();
	}
	
	public static <T> T getBean(Class<T> beanClass) {
		
		T bean = context.getBean(beanClass);		
		
		return bean;
	}
	
	public static Object getBean(String beanId) {
		
		Object bean = context.getBean(beanId);		
		
		return bean;
	}
	
}
