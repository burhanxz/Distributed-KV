package lsm;

import io.netty.buffer.ByteBuf;

public interface Block extends SeekingIterable<ByteBuf, ByteBuf> {
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
