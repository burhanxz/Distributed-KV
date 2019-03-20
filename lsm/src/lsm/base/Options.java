package lsm.base;

import java.util.Comparator;

//TODO
public class Options {
	// TODO
	public static final Comparator<InternalKey> INTERNAL_KEY_COMPARATOR = new InternalKeyComparator();
	public static final String INTERNAL_KEY_COMPARATOR_NAME = "default_comparator";
	public static final String FILTER = "Bloom filter";
	public static final int MEMTABLE_LIMIT = 1 << 26; // memtable 大小限制64MB
	public static final int L0_SLOW_DOWN_COUNT = 8; // 开始延缓时，level0层的文件数量
	/**
	 * 最大层数
	 */
	public static final int LEVELS = 7;
	/**
	 * level0 score计算的基数
	 */
	public static final int LEVEL0_SCORE_BASE = 4;
	/**
	 * level0 文件上限
	 */
	public static final int LEVEL0_LIMIT_COUNT = 12;
	/**
	 * 一次最多合并32个写任务
	 */
	public static final int WRITE_BATCH_LIMIT_SIZE = 1 << 5;
	/**
	 * table文件缓存的大小
	 */
	public static final int TABLE_CACHE_SIZE = 1 << 10;
}
