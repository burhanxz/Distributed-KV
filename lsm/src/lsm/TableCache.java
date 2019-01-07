package lsm;

import lsm.base.LookupKey;
import lsm.base.LookupResult;

/**
 * LRU规则下的table文件缓存，加快table读取
 * @author bird
 *
 */
public interface TableCache {
	/**
	 * 根据table对应的文件编号获取table
	 * 
	 * @param fileNumber
	 *            文件编号
	 * @return sstable实体
	 * @throws Exception
	 */
	public SSTable getTable(long fileNumber) throws Exception;

	/**
	 * 根据key查找result
	 * 
	 * @param key
	 * @return
	 * @throws Exception 
	 */
	public LookupResult get(LookupKey key, long fileNumber, long fileSize) throws Exception;
	/**
	 * 主动从缓存中清除table
	 * @param number table对应的文件编号
	 */
	public void evict(long number);
}
