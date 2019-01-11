package lsm;

import java.io.IOException;
import java.util.Map;

import lsm.base.Compaction;
import lsm.base.InternalKey;
import lsm.internal.VersionEdit;

public interface VersionSet {
	/**
	 * 当前系统是否需要compact
	 * @return
	 */
	public boolean needsCompaction();
	
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
	 * 将指定compact pointers数据设置进version set的compact pointers中
	 * @param compactionPointers4Set
	 */
	public void setCompactPointers(Map<Integer, InternalKey> compactionPointers4Set);
}
