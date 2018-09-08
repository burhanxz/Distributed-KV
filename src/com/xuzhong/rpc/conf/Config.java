package com.xuzhong.rpc.conf;

/**
 * @author bird
 * （暂时）利用此类进行一些全局性配置 
 */
public class Config {
	private static String distributedLockImpl = "RedisDistributedLock";
	
	public static void setDistributedLockImpl(String s) {
		distributedLockImpl = s;
	}
	
	public static String getDistributedLockImpl() {
		return distributedLockImpl;
	}
}
