package lsm;

import lsm.base.LookupKey;
import lsm.base.LookupResult;

public interface TableCache {
	/**
	 * 根据table对应的文件编号获取table
	 * @param fileNumber 文件编号
	 * @return sstable实体
	 */
	public SSTable getTable(long fileNumber);
	/**
	 * 根据key查找result
	 * @param key
	 * @return
	 */
	public LookupResult get(LookupKey key, long fileNumber, long fileSize);
}
