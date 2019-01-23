package rpc.model;

/**
 * 请求数据模型信息
 * 作为键名存放在invocation的attachments缓存中
 * @author bird
 *
 */
public class RequestConstants {
	public static final String LOAD_BALANCE = "loadBalance";
	public static final String IS_ASYNC = "isAsync"; 
	public static final String UNIQUEKEY = "uniqueKey";
	public static final String TIMEOUT = "timeout";
	public static final String APP = "app";
	public static final String RETRY = "retry";
	// 默认值
	public static int TIMEOUT_DEFAULT = 5000; //ms
}
