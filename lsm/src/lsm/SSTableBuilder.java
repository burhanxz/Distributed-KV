package lsm;

import java.io.IOException;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

public interface SSTableBuilder {
	/**
	 * 加入k-v对到sstable中
	 * @param entry
	 * @throws IOException 
	 */
	public void add(Entry<InternalKey, ByteBuf> entry) throws IOException;
	
	/**
	 * 添加数据到tableBuilder中
	 * @param key
	 * @param value
	 * @throws IOException 
	 */
	public void add(ByteBuf key, ByteBuf value) throws IOException;
	
	/**
	 * 结束添加数据到builder中
	 * @throws IOException 
	 */
	public void finish() throws IOException;
	
	/**
	 * 禁止再添加数据到builder中
	 */
	public void abandon();

	/**
	 * 获取当前文件实际大小
	 * @return
	 * @throws IOException 
	 */
	public long getFileSize() throws IOException;
}
