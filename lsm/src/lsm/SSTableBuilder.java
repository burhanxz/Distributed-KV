package lsm;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

public interface SSTableBuilder {
	/**
	 * 加入k-v对到sstable中
	 * @param entry
	 */
	public void add(Entry<InternalKey, ByteBuf> entry);
	
	/**
	 * 添加数据到tableBuilder中
	 * @param key
	 * @param value
	 */
	public void add(ByteBuf key, ByteBuf value);
	
	/**
	 * 结束添加数据到builder中
	 */
	public void finish();
	
	/**
	 * 禁止再添加数据到builder中
	 */
	public void abandon();

	/**
	 * 获取当前文件实际大小
	 * @return
	 */
	public long getFileSize();
}
