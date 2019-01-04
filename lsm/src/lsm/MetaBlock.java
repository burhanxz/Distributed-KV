package lsm;

import io.netty.buffer.ByteBuf;

public interface MetaBlock {
	/**
	 * 判断某block中是否存在key
	 * @param blockOffset 此block在sstable中的位置
	 * @param key 
	 * @return 如果存在则必然返回true，不存在则小概率情况下返回true
	 */
	public boolean keyMayMatch(int blockOffset, ByteBuf key);
}
