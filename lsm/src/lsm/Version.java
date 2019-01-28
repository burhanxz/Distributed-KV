package lsm;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import lsm.base.FileMetaData;
import lsm.base.InternalKey;
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
	 * @throws Exception 
	 */
	public LookupResult get(LookupKey key) throws Exception;
	/**
	 * 寻找第level层和指定范围有重叠的全部文件信息
	 * @param level 层数
	 * @param smallest 最小值
	 * @param largest 最大值
	 * @return 所有重叠文件
	 */
	public List<FileMetaData> getOverlappingInputs(int level, InternalKey smallest, InternalKey largest);
	/**
	 * 获取引用计数
	 * @return
	 */
	public int refs();
	/**
	 * 增加一次引用计数
	 */
	public void retain();
	/**
	 * 减少一次引用计数
	 */
	public void release();
	/**
	 * 向指定层级加入文件
	 * @param level
	 * @param fileMetaData
	 */
	public void addFile(int level, FileMetaData fileMetaData);	
	/**
	 * 获取某层所有file信息
	 * @param level 层数
	 * @return 所有文件信息
	 */
	public List<FileMetaData> getFiles(int level);
	/**
	 * 获取所有层的所有文件信息
	 * @return
	 */
	public default Map<Integer, List<FileMetaData>> getFiles(){
		ImmutableMap.Builder<Integer, List<FileMetaData>> mapBuilder = ImmutableMap.builder();
		// 遍历所有层次
		for(int i = 0; i < VersionSet.MAX_LEVELS; i++) {
			mapBuilder.put(i, getFiles(i));
		}
		return mapBuilder.build();
	}
	/**
	 * 计算某层的总数据量
	 * @param level 层数
	 * @return 总数据量
	 */
	public default long levelBytes(int level) {
		long levelBytes = 0;
		for (FileMetaData fileMetaData : getFiles(level)) {
			levelBytes += fileMetaData.getFileSize();
		}
		return levelBytes;
	}
	/**
	 * 某层中的文件总数
	 * @param level 层编号
	 * @return 文件数目
	 */
	public int files(int level);
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
