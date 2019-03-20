package lsm.internal;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import lsm.Current;
import lsm.LogReader;
import lsm.LogWriter;
import lsm.TableCache;
import lsm.Version;
import lsm.VersionBuilder;
import lsm.VersionSet;
import lsm.base.Compaction;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;
import lsm.base.InternalKey;
import lsm.base.Options;

public class VersionSetImpl implements VersionSet{

	/**
	 * manifest 文件编号为1
	 */
	private final static long INIT_MANIFEST_FILENUM = 1;
	/**
	 * 除manifest以外的文件，编号从2开始
	 */
	private final AtomicLong nextFileNumber = new AtomicLong(INIT_MANIFEST_FILENUM + 1);
	/**
	 * 数据库工作目录
	 */
	private final File databaseDir;
	private Current current;
	/**
	 * manifest，也称为描述日志
	 */
	private LogWriter manifest;
	/**
	 * manifest文件编号
	 */
	private long manifestFileNumber = INIT_MANIFEST_FILENUM;
	/**
	 * 全局唯一table缓存
	 */
	private final TableCache tableCache;
	/**
	 * 存放所有version
	 */
	private final Map<Version, Object> activeVersions = new WeakHashMap<>();
	/**
	 * 当前version
	 */
	private Version currentVersion;
	// edit信息
	private long lastSequence;
	private long logNumber;
	private final Map<Integer, InternalKey> compactPointers = new TreeMap<>();
	
	public VersionSetImpl(File databaseDir, int cacheSize) throws IOException {
		Preconditions.checkNotNull(databaseDir);
		Preconditions.checkArgument(databaseDir.exists() && databaseDir.isDirectory());
		Preconditions.checkArgument(cacheSize > 0);
		this.databaseDir = databaseDir;
		// 指定缓存文件数量，初始化tablecache
		this.tableCache = new TableCacheImpl(databaseDir, cacheSize);
		// 初始化空version作为current
		Version initialVersion = new VersionImpl(this);
		appendVersion(initialVersion);
		this.current = new CurrentImpl(databaseDir);
		// 如果current文件不存在，则初始化current
		if(!current.exists()) {
			initCurrent();
		}
	}
	
	@Override
	public void recover() throws IOException {
		Preconditions.checkNotNull(current);
		Preconditions.checkState(current.exists());
		// 读取current文件中的manifest文件信息
		String manifestName = current.getManifest();
		
		File manifestFile = new File(databaseDir, manifestName);
		// 新建version builder
		VersionBuilder builder = new VersionBuilderImpl(this, currentVersion);
		// 逐一获取其中的versionEdit信息
		try(LogReader logReader = new LogReaderImpl(manifestFile)){
			for(ByteBuf record = logReader.readNextRecord(); record != null; record = logReader.readNextRecord()) {
				VersionEdit edit = new VersionEdit(record);
				// 将edit逐一应用到version builder中
				builder.apply(edit);
				// 更新log number, last seq和 next file number
				logNumber = edit.getLogNumber() >= 0 ? edit.getLogNumber() : logNumber;
				lastSequence = edit.getLastSequenceNumber() >= 0 ? edit.getLastSequenceNumber() : lastSequence;
				if(edit.getNextFileNumber() >= nextFileNumber.get()) {
					nextFileNumber.set(edit.getNextFileNumber());
				}
			}
		}
		// version builder获取version
		Version version = builder.build();
		setLevelAndScore(version);
		// 更新version列表
		appendVersion(version);
		// 分配一个manifest文件编号，并且更新next file number
		manifestFileNumber = nextFileNumber.getAndIncrement();
		
	}
	
	@Override
	public boolean needsCompaction() {
		// 如果当前version的score大于等于1则说明需要进行compaction操作
		return currentVersion.getCompactionScore() >= 1;
	}

	@Override
	public Compaction pickCompaction() {
		Preconditions.checkState(needsCompaction());
		// 获取levelInputs,level和levelUpInputs
		int level;
		List<FileMetaData> levelInputs = new ArrayList<>();
		List<FileMetaData> levelUpInputs = new ArrayList<>();
		// 通过version获取compaction level
		level = currentVersion.getCompactionLevel();
		Preconditions.checkState(level >= 0);
		Preconditions.checkState(level + 1 < VersionSet.MAX_LEVELS);
		// 遍历level层所有的文件，寻找第一个可compact的文件
		Preconditions.checkState(!currentVersion.getFiles(level).isEmpty());
		for (FileMetaData fileMetaData : currentVersion.getFiles(level)) {
			// 一个文件如果最大值大于合并点，或者没有合并点，则可以选择作为level inputs
			if (!compactPointers.containsKey(level)
					|| Options.INTERNAL_KEY_COMPARATOR.compare(fileMetaData.getLargest(), compactPointers.get(level)) > 0) {
				levelInputs.add(fileMetaData);
				break;
			}
		}
		// 如果没有合适文件，使用第一个文件即可
		if (levelInputs.isEmpty()) {
			FileMetaData fileMetaData = currentVersion.getFiles(level).get(0);
			levelInputs.add(fileMetaData);
		}
		// 如果是第0层，还要把所有和上述levelInputs中选中的文件有重叠的文件再次选入
		if(level == 0) {
			// 获取大小范围
			Entry<InternalKey, InternalKey> range = getRange(levelInputs);
			// 按照大小范围在level0中查找重叠文件
			levelInputs = getOverlappingInputs(0, range.getKey(), range.getValue());
		}
		// 遍历level+1层文件，寻找所有和指定范围有重叠的文件
		Preconditions.checkState(!levelInputs.isEmpty());
		// 重新获取level层大小范围
		Entry<InternalKey, InternalKey> range = getRange(levelInputs);
		// 按照level层所有文件大小范围，从level+1层获取重叠文件
		levelUpInputs = getOverlappingInputs(level + 1, range.getKey(), range.getValue());
		// 尝试在不改变levelUpInputs即level + 1层文件大小的情况下，增加level层选中文件数目
		// 再次扩大小大范围，将level层和level+1层所有文件的大小范围计算进去
		range = getRange(levelInputs, levelUpInputs);
		// 按照新的大小范围再去level层中寻找重叠文件
		List<FileMetaData> levelInputsTmp = getOverlappingInputs(level, range.getKey(), range.getValue());
		// 检验新的大小范围是否导致level + 1层选中文件增多
		if(levelInputsTmp.size() > levelInputs.size()) {
			// 更新大小范围
			range = getRange(levelInputsTmp);
			// 再次寻找level+1层中重叠文件
			List<FileMetaData> levelUpInputsTmp = getOverlappingInputs(level + 1, range.getKey(), range.getValue());
			// 如果level+1选中文件没有增多，则说明level层文件扩容是有效的
			if(levelUpInputs.size() == levelUpInputsTmp.size()) {
				// 更新level层和level+1层选中文件
				levelUpInputs = levelUpInputsTmp;
				levelInputs = levelInputsTmp;
			}
		}
		// 新建compaction
		Compaction compaction = new Compaction(currentVersion, level, levelInputs, levelUpInputs);
		//更新level层的compact pointer
		range = getRange(levelInputs);
		InternalKey levelLargest = range.getValue();
		compactPointers.put(level, levelLargest);
		// 更新edit中的compact nullpointer
		compaction.getEdit().setCompactPointer(level, levelLargest);
		return compaction;
	}

	@Override
	public void logAndApply(VersionEdit edit) throws IOException {
		// edit信息设置到versionSet字段中
		// 如果edit中已经包含一些信息,检查这些信息
		if (edit.getLogNumber() != null) {
			checkArgument(edit.getLogNumber() >= logNumber);
			checkArgument(edit.getLogNumber() < nextFileNumber.get());
		} else {
			edit.setLogNumber(logNumber);
		}
		edit.setNextFileNumber(nextFileNumber.get());
		edit.setLastSequenceNumber(lastSequence);
		
		// 利用versionBuilder和edit，经过多次中间形态,形成最终的version
		VersionBuilder builder = new VersionBuilderImpl(this, currentVersion);
		builder.apply(edit);
		Version version = builder.build();
		// 对version中的每一级sstable做一个评估，选择score最高的level作为需要compact的level（compaction_level_），评估是根据每个level上文件的大小（level 你，n>0）和数量(level 0)
		setLevelAndScore(version);
		// 将edit信息持久化到manifest
		boolean newManifest = false;
		if(manifest == null) {
			edit.setNextFileNumber(nextFileNumber.get());
			// 新建manifest日志
			manifest = new LogWriterImpl(databaseDir, manifestFileNumber, true);
			// 记录当前version信息
			VersionEdit tmpEdit = new VersionEdit();
			tmpEdit.setComparatorName(Options.INTERNAL_KEY_COMPARATOR_NAME);
			tmpEdit.setCompactPointers(compactPointers);
			tmpEdit.addFiles(currentVersion.getFiles());
			// 序列化edit信息
			ByteBuf record = tmpEdit.encode();
			// 将edit信息写入日志
			manifest.addRecord(record, false);
			newManifest = true;
		}
		// 将现在的edit序列化并写入manifest
		ByteBuf record = edit.encode();
		manifest.addRecord(record, true);
		// 将manifest文件信息记录到current文件中
		if(newManifest) {
			current.setManifest(manifestFileNumber);
		}
		// 把得到的version最终放入versionSet中
		appendVersion(version);
		logNumber = edit.getLogNumber();
	}
	/**
	 * 将snapshot信息写入日志
	 * snapshot即当前时间点的状态，包括comparator, compact pointers和new files三类信息
	 * @param 日志
	 * @throws IOException 
	 */
	private void writeSnapShot(LogWriter logWriter) throws IOException {
		// 新建edit
		VersionEdit edit = new VersionEdit();
		// 依次添加comparator, compact pointers和new files三类信息
		edit.setComparatorName(Options.INTERNAL_KEY_COMPARATOR_NAME);
		edit.setCompactPointers(compactPointers);
		edit.addFiles(currentVersion.getFiles());
		// 序列化edit并写入日志
		logWriter.addRecord(edit.encode(), false);
	}
	/**
	 * 初始化current文件
	 * @throws IOException 
	 */
	private void initCurrent() throws IOException {
		// 新建log writer用于manifest日志
		try(LogWriter log = new LogWriterImpl(databaseDir, manifestFileNumber, true)){
			// 写入快照信息到manifest.由于current丢失，manifest作废，需要写入快照信息来作为manifest初始信息
			writeSnapShot(log);
			// 新建edit
			VersionEdit edit = new VersionEdit();
			// 记录comparator, log number, next file number, last seq四类基本信息 
			edit.setComparatorName(Options.INTERNAL_KEY_COMPARATOR_NAME);
			edit.setLogNumber(logNumber);
			edit.setNextFileNumber(nextFileNumber.get());
			edit.setLastSequenceNumber(lastSequence);
			// 写入新的edit信息到manifest
			log.addRecord(edit.encode(), false);
			// 设置manifest信息到current
			current.setManifest(manifestFileNumber);
		}
	}

	/**
	 * 将version指定为current，存入version列表
	 * @param version
	 */
	private void appendVersion(Version version) {
		Preconditions.checkNotNull(version);
		Preconditions.checkArgument(version.refs() == 0);
		Preconditions.checkArgument(version != currentVersion);
		// 减少current引用计数，如果引用计数降为0则主动删除version
		if(currentVersion != null) {
			currentVersion.release();
			if(currentVersion.refs() == 0) {
				activeVersions.remove(currentVersion);
			}
		}
		// 更新current并放入version列表
		currentVersion = version;
		currentVersion.retain();
		activeVersions.put(currentVersion, new Object());
	}
	private void setLevelAndScore(Version version) {
		// 最佳level和最佳score
		int bestLevel = -1;
		double bestScore = -1.0;
		// 遍历所有层次，找出最大的score值并记录
		for (int level = 0; level < VersionSet.MAX_LEVELS; level++) {
			double score = 0;
			if (level == 0) {
				// level0 依据文件数目
				score = 1.0 * version.files(level) / Options.LEVEL0_SCORE_BASE;
			} else {
				// level1以上的层次，依据文件大小
				long levelBytes = version.levelBytes(level);
				score = (double)levelBytes / VersionSet.maxBytesForLevel(level);
			}
			// 更新最佳score和level
			if (score > bestScore) {
				bestLevel = level;
				bestScore = score;
			}
		}
		// 将最佳level和score设置到version
		version.setCompactionLevel(bestLevel);
		version.setCompactionScore(bestScore);
	}
	/**
	 * 获取所有文件的最终大小范围
	 * @param inputLists
	 * @return 最小值和最大值
	 */
	private Entry<InternalKey, InternalKey> getRange(List<FileMetaData>... inputLists) {
		InternalKey smallest = null;
		InternalKey largest = null;
		// 遍历所有文件列表中的所有文件
		for (List<FileMetaData> inputList : inputLists) {
			for (FileMetaData fileMetaData : inputList) {
				if (smallest == null) {
					smallest = fileMetaData.getSmallest();
					largest = fileMetaData.getLargest();
				} else {
					// 比较获取最值
					if (Options.INTERNAL_KEY_COMPARATOR.compare(fileMetaData.getSmallest(), smallest) < 0) {
						smallest = fileMetaData.getSmallest();
					}
					if (Options.INTERNAL_KEY_COMPARATOR.compare(fileMetaData.getLargest(), largest) > 0) {
						largest = fileMetaData.getLargest();
					}
				}
			}
		}
		Preconditions.checkNotNull(smallest);
		Preconditions.checkNotNull(largest);
		return Maps.immutableEntry(smallest, largest);
	}
	/**
	 * 寻找第level层和指定范围有重叠的全部文件信息
	 * @param level 层数
	 * @param smallest 最小值
	 * @param largest 最大值
	 * @return 所有重叠文件
	 */
	private List<FileMetaData> getOverlappingInputs(int level, InternalKey smallest, InternalKey largest){
		Preconditions.checkNotNull(smallest);
		Preconditions.checkNotNull(largest);
		Preconditions.checkNotNull(smallest.getUserKey());
		Preconditions.checkNotNull(largest.getUserKey());
		ImmutableList.Builder<FileMetaData> files = ImmutableList.builder();
		// 遍历level0层文件，寻找和上述范围有重叠的文件
		for (FileMetaData fileMetaData : currentVersion.getFiles(level)) {
			if(!(fileMetaData.getLargest().getUserKey().compareTo(smallest.getUserKey()) < 0
					|| fileMetaData.getSmallest().getUserKey().compareTo(largest.getUserKey()) > 0)) {
				files.add(fileMetaData);
			}
		}
		return files.build();
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
		return currentVersion;
	}
	
	@Override
	public TableCache getTableCache() {
		return tableCache;
	}
	
	@Override
	public void setCompactPointers(Map<Integer, InternalKey> compactionPointers4Set) {
		compactPointers.putAll(compactionPointers4Set);
	}

	@Override
	public String toString() {
		return "VersionSetImpl [nextFileNumber=" + nextFileNumber.get() + ", databaseDir=" + databaseDir
				+ ", manifestFileNumber=" + manifestFileNumber + ", activeVersions=" + activeVersions
				+ ", currentVersion=" + currentVersion + ", lastSequence=" + lastSequence + ", logNumber=" + logNumber
				+ ", compactPointers=" + compactPointers + "]";
	}

	@Override
	public void setLastSequence(long seq) {
		Preconditions.checkArgument(seq >= lastSequence);
		lastSequence = seq;
	}


}
