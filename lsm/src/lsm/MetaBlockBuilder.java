package lsm;

import io.netty.buffer.ByteBuf;

/**
 * meta block 结构
 * | filters(各个filter数据) | filter offsets(各个filter数据的大小) | sum(filter数据总大小) | kFilterBaseLg(基数，代表多少字节的data block产生一个meta block) |  
 * @author bird
 *
 */
public interface MetaBlockBuilder {
	/**
	 * 记录前一个data block的filter信息
	 * @param blockOffset 下一个data block的offset
	 */
	public void startBlock(int blockOffset);
	/**
	 * 添加key
	 * @param key
	 */
	public void addKey(ByteBuf key);
	/**
	 * 返回meta block的字节数据
	 * @return
	 */
	public ByteBuf finish();
}
