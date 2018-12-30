package lsm;

import io.netty.buffer.ByteBuf;

public interface SSTableBlock extends SeekingIterable<ByteBuf, ByteBuf> {
	/**
	 * block中的record的数目*
	 * 
	 * @return record的数目
	 */
	public int size();

	/**
	 * block在table中的偏移
	 * 
	 * @return 偏移量
	 */
	public long getOffset();

	/**
	 * block有效数据大小（除block trailer）
	 * 
	 * @return
	 */
	public int getDataSize();

	/**
	 * block实际大小，包括block trailer
	 * 
	 * @return
	 */
	public int getFullBlockSize();
}
