package lsm;

import io.netty.buffer.ByteBuf;

public interface Version {
	/**
	 * 根据key的大小范围，查找得到最高的层级。此层级满足条件：不存在这样的sstable文件，它的key范围与提供的范围有重叠
	 * @param smallest key范围下界
	 * @param largest key范围上界
	 * @return 满足条件的最高层级
	 */
	public int pickLevelForMemTableOutput(ByteBuf smallest, ByteBuf largest);
}
