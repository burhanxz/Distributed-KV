package rpc.data;

import java.lang.reflect.Method;
import java.util.Arrays;

public class IRequest {
	private long id;
	private Object[] parameters;
	private Class<?> interfaceClazz;
	private Method method;
	
	public IRequest() {}
	
	public IRequest(long id, Class<?> interfaceClazz, Method method, Object[] parameters) {
		
		this.id = id;
		this.parameters = parameters;
		this.interfaceClazz = interfaceClazz;
		this.method = method;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}

	public Class<?> getInterfaceClazz() {
		return interfaceClazz;
	}

	public void setInterfaceClazz(Class<?> interfaceClazz) {
		this.interfaceClazz = interfaceClazz;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}

	@Override
	public String toString() {
		return "IRequest [id=" + id + ", parameters=" + Arrays.toString(parameters) + ", interfaceClazz="
				+ interfaceClazz + ", method=" + method + "]";
	}


	
	
}
