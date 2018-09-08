package com.xuzhong.rpc.lock;

/**
 * @author bird
 * 分布式锁
 * 分布式锁本质上都是利用公共的外部资源来实现互斥性操作
 */
public interface DistributedLock {
	
	/**
	 * @param lockName redis key的名字，即锁名
	 * @return 返回一个lockSerialNumber（uuid）或null，如果加锁成功，则为uuid，以此为凭据进行解锁动作
	 */
	public String lock(String lockName);
	
	/**
	 * @param lockName redis key的名字，即锁名
	 * @param lockSerialNumber 锁序列号码，是加锁时获取的标识符（uuid），作为解锁的凭据
	 * @return 解锁成功则为true，否则为false
	 */
	public boolean unLock(String lockName, String lockSerialNumber);

}
