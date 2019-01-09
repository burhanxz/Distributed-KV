package rpc.data;

/**
 * represent a response from remote server
 * 
 * @author bird
 *
 */
public class IResponse {
	/*
	 * unique id. length: 16
	 */
	private long id;
	private Object returnValue;

	public IResponse() {
	}

	public IResponse(long id, Object[] returnValue) {
		super();
		this.id = id;
		this.returnValue = returnValue;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Object getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(Object returnValue) {
		this.returnValue = returnValue;
	}

	@Override
	public String toString() {
		return "IResponse [id=" + id + ", returnValue=" + returnValue + "]";
	}



}
