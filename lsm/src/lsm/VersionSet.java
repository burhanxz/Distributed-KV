package lsm;

import java.io.IOException;
import java.util.Map;

import lsm.base.Compaction;
import lsm.base.InternalKey;
import lsm.internal.VersionEdit;

public interface VersionSet {
	/**
	 * LSM中最多分7层
	 */
	public static int MAX_LEVELS = 7;
	/**
	 * 当前系统是否需要compact
	 * @return
	 */
	public boolean needsCompaction();
	/**
	 * 恢复数据库信息
	 * @throws IOException 
	 */
	public void recover() throws IOException;
	/**
	 * 获取compaction信息
	 * @return
	 */
	public Compaction pickCompaction();
	
	/**
	 * 记录和应用一个edit，来产生一个新的version
	 * @param edit 
	 * @throws IOException 
	 */
	public void logAndApply(VersionEdit edit) throws IOException;
	/**
	 * 计算每层最大数据量
	 * @param level 层数
	 * @return 最大数据量
	 */
	public static double maxBytesForLevel(int level) {
		// base单位: MB
		double base = 10.0;
		double bytes = 0;
		// level0层文件大小为10MB
		if(level == 0) {
			bytes = base;
		}
		// level1以上，每层为10 ^ level MB
		else if(level > 0 && level <= 7) {
			bytes = Math.pow(base, level);
		}
		return bytes * (1 << 20);
	}
	/**
	 * 获取最新序列号
	 * @return
	 */
	public long getLastSequence();
	
	/**
	 * 获取下一个文件编号
	 * @return
	 */
	public long getNextFileNumber();
	/**
	 * 获取当前版本
	 * @return
	 */
	public Version getCurrent();
	/**
	 * 获取tableCache
	 * @return
	 */
	public TableCache getTableCache();
	/**
	 * 将指定compact pointers数据设置进version set的compact pointers中
	 * @param compactionPointers4Set
	 */
	public void setCompactPointers(Map<Integer, InternalKey> compactionPointers4Set);
}
