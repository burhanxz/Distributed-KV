package lsm.internal;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.MemTable;
import lsm.SeekingIterator;
import lsm.base.InternalKey;
import lsm.base.InternalKey.InternalKeyType;
import lsm.base.LookupKey;
import lsm.base.LookupResult;
import lsm.base.Options;

public class MemTableImpl implements MemTable{
	/**
	 * memtable核心结构 跳表
	 */
	private ConcurrentSkipListMap<InternalKey, ByteBuf> skipList;
	/**
	 * memtable中当前存储的数据的总大小
	 */
	private AtomicLong size;
	private MemTableImpl() {
		skipList = new ConcurrentSkipListMap<>(Options.INTERNAL_KEY_COMPARATOR);
	}
	@Override
	public Iterator<Entry<InternalKey, ByteBuf>> iterator() {
		return skipList.entrySet().iterator();
	}
	@Override
	public void add(long seq, InternalKeyType type, ByteBuf key, ByteBuf value) {
		// 新建internal key
		InternalKey internalKey = new InternalKey(key, seq, type);
		// 计算数据增量
		int increment = internalKey.size() + value.readableBytes();
		// 更新数据大小
		size.getAndAdd(increment);
		// 插入数据到跳表中
		skipList.put(internalKey, value);
	}
	@Override
	public long size() {
		return size.get();
	}

	@Override
	public boolean isEmpty() {
		return size.get() == 0;
	}

	@Override
	public LookupResult get(LookupKey key) {
		Preconditions.checkNotNull(key);
		// 获取skiplist中第一个 >= key的数据
		Entry<InternalKey, ByteBuf> entry = skipList.ceilingEntry(key.getInternalKey());
		// 如果内置bytebuf相等，则找到数据
		if(entry != null && entry.getKey().getUserKey().equals(key.getUserKey())) {
			return new LookupResult(key, entry.getValue(), entry.getKey().getType());
		}
		return null;
	}


}
