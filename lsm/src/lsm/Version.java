package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

/**
 * 版本，实际上是管理一个版本内所有文件信息
 * @author bird
 *
 */
public interface Version {	
	/**
	 * 核心方法，获取key对应的value
	 * @param key 查找的key
	 * @return 查到的value
	 */
	public LookupResult get(LookupKey key);
	/**
	 * 增加一次引用计数
	 */
	public void retain();
	/**
	 * 减少一次引用计数
	 */
	public void release();

	/**
	 * 获取compaction score，是判断是否需要compaction的关键指标
	 * @return
	 */
	public double getCompactionScore();
	public void setCompactionScore(double score);
	public int getCompactionLevel();
	public void setCompactionLevel(int level);
	/**
	 * 根据key的大小范围，查找得到最高的层级。此层级满足条件：不存在这样的sstable文件，它的key范围与提供的范围有重叠
	 * @param smallest key范围下界
	 * @param largest key范围上界
	 * @return 满足条件的最高层级
	 */
//	public int pickLevelForMemTableOutput(ByteBuf smallest, ByteBuf largest);
	
}
