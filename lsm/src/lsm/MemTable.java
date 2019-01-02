package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

// TODO
public interface MemTable extends SeekingIterable<InternalKey, ByteBuf>{
	/**
	 * 获取memtable大小
	 * @return
	 */
	public int size();
	
	/**
	 * 判断memtable是否为空
	 * @return
	 */
	public boolean isEmpty();
	
	/**
	 * 获取value
	 * @param key 键
	 * @return
	 */
	public ByteBuf get(InternalKey key);
}
