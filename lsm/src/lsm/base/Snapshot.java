package lsm.base;

import java.io.Closeable;
import java.io.IOException;

import lsm.Version;

/**
 * 快照，用于MVCC并发版本控制，同时也涉及version的释放管理
 * @author bird
 *
 */
public class Snapshot implements Closeable{
	/**
	 * 标识，防止反复close
	 */
	private volatile boolean isClosed;
	/**
	 * 快照对应的版本
	 */
	private final Version current;
	public Snapshot(Version current) {
		this.isClosed = false;
		this.current = current;
		// 增加version引用
		current.retain();
	}
	/**
	 * 返回快照对应的版本
	 * @return
	 */
	public Version getVersion() {
		return current;
	}
	/**
	 * MVCC
	 * @param key
	 * @return 
	 * @throws Exception
	 */
	public LookupResult get(LookupKey key) throws Exception {
		// 在快照所拥有的版本中寻找key并返回结果
		return current.get(key);
	}
	
	@Override
	public synchronized void close() throws IOException {
		if(isClosed)
			return;
		// 释放version
		current.release();
		// 修改标识位防止反复close
		isClosed = true;
	}
}
