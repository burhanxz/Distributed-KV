package rpc.lock;

import java.util.UUID;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import redis.clients.jedis.exceptions.JedisException;
import rpc.redis.RedisPoolUtil;

/**
 * @author bird
 * redis方式实现的分布式锁
 * 原理是利用setnx动作。由于setnx只能在值不存在的时候写入，一旦写入值之后就无法再写入，
 * 不同节点上的相同方法被调用时，可利用setnx动作来模拟互斥操作
 * 
 */
public class RedisDistributedLock implements DistributedLock{

	// 最多持有锁20s
	private final static int holdLockTimeOut = 20;
	// 最多尝试获取锁10000ms，防止死锁
	private final static long tryGetLockTimeOut = 10000;

	private final static JedisPool pool = RedisPoolUtil.getPool();

	//static final Jedis 
	

	// private String lockData;
	// 加锁
	/* 
	 * @see com.xuzhong.rpc.lock.DistributedLock#lock(java.lang.String)
	 */
	@Override
	public String lock(String lockName) {
		String lock = "lock:" + lockName;
		String uuid = UUID.randomUUID().toString();
		Long end = System.currentTimeMillis() + tryGetLockTimeOut;
		//System.out.println(Thread.currentThread().getName() + ": " + uuid);
		Jedis conn = pool.getResource();

		try {
			while (System.currentTimeMillis() < end) {
				// 获取锁成功，设置key的删除时间为holdLockTimeOut，使得锁可以在一段时间之后释放
				if (conn.setnx(lock, uuid) == 1) {
					// 设置超时删除，同时设置好lockData
					conn.expire(lock, holdLockTimeOut);
					System.out.println("+++++++++++++++++++");
					System.out.println(Thread.currentThread().getName() + "redis锁加锁成功");
					return uuid;
				}

				// 给予redis操作时间，防止反复使用setnx指令时出现不可知错误
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}

			}
			// 获取失败
			return null;
		} finally {
			conn.close();
		}
	}

	// 解锁
	/* (non-Javadoc)
	 * @see com.xuzhong.rpc.lock.DistributedLock#unLock(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean unLock(String lockName, String lockSerialNum) {
		boolean result = false;
		String lock = "lock:" + lockName;
		
		Jedis conn = pool.getResource();
		// 循环尝试解锁
		// while循环适用于超时剥夺锁的情况
		while (true) {
			try {
				// 监视锁数据，一旦被人篡改则重试
				conn.watch(lock);
				// 当确认是自己上的锁时，利用实务进行解锁操作
				if (conn.get(lock).equals(lockSerialNum)) {
					Transaction transaction = conn.multi();
					transaction.del(lock);
					// 执行操作可能有隐患
					transaction.exec();
					result = true;
					System.out.println(Thread.currentThread().getName() + "redis锁解锁成功");
					System.out.println("+++++++++++++++++++");
				}
				conn.unwatch();
				break;
			} catch (JedisException e) {
				// conn.watch(lock)触发，重试解锁
				System.out.println(Thread.currentThread().getName() + "redis锁已被篡改，正在重试...");
				System.out.println("+++++++++++++++++++");
				// e.printStackTrace();
			} finally {
				// 关闭redis连接
				conn.close();
			}

		}

		return result;
	}


}
