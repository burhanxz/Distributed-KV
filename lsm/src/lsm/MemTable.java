package lsm;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;
import lsm.base.InternalKey.InternalKeyType;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

// TODO
public interface MemTable extends Iterable<Entry<InternalKey, ByteBuf>>{
	public void add(long seq, InternalKeyType type, ByteBuf key, ByteBuf value);
	/**
	 * 获取memtable数据量大小, 单位: B
	 * @return
	 */
	public long size();
	
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
	public LookupResult get(LookupKey key);
	public void add(InternalKey internalKey, ByteBuf value);
}
