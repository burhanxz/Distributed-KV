package lsm.internal;

import java.io.File;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.collect.MapMaker;

import lsm.Version;
import lsm.VersionSet;
import lsm.base.Compaction;
import lsm.base.InternalKey;

public class VersionSetImpl implements VersionSet{
	/**
	 * manifest 文件编号为1
	 */
	private final static long MANIFEST_FILENUM = 1;
	/**
	 * 除manifest以外的文件，编号从2开始
	 */
	private final AtomicLong nextFileNumber = new AtomicLong(2);
	/**
	 * 数据库工作目录
	 */
	private final File databaseDir;
	/**
	 * manifest，也称为描述日志
	 */
	private LogWriter manifest;
//	private final TableCache tableCache;
	/**
	 * internak key 的比较器
	 */
	private final Comparator<InternalKey> internalKeyComparator;
	/**
	 * 存放所有version
	 */
	private final Map<Version, Object> activeVersions = new WeakHashMap<>();
	/**
	 * 当前version
	 */
	private Version current;
	// edit信息
	private long lastSequence;
	private long logNumber;
	private final Map<Integer, InternalKey> compactPointers = new TreeMap<>();
	
	public VersionSetImpl(File databaseDir, Comparator<InternalKey> internalKeyComparator) {
		// TODO
		this.databaseDir = databaseDir;
		this.internalKeyComparator = internalKeyComparator;
	}
	@Override
	public boolean needsCompaction() {
//		return current.getCompactionScore() >= 1 || current.getFileToCompact() != null;
		//TODO
		return false;
	}

	@Override
	public Compaction pickCompaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logAndApply(VersionEdit edit) {
		// TODO Auto-generated method stub
		// edit信息设置到versionSet字段中
		// 利用versionBuilder和edit，经过多次中间形态,形成最终的version
		// 对version中的每一级sstable做一个评估，选择score最高的level作为需要compact的level（compaction_level_），评估是根据每个level上文件的大小（level 你，n>0）和数量(level 0)
		// 将edit信息持久化到manifest
		// 把得到的version最终放入versionSet中
	}

	@Override
	public long getLastSequence() {
		return lastSequence;
	}

	@Override
	public long getNextFileNumber() {
		return nextFileNumber.getAndIncrement();
	}

	@Override
	public Version getCurrent() {
		return current;
	}



}
