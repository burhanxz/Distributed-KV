package lsm.internal;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;

import lsm.Level;
import lsm.Version;
import lsm.VersionSet;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

public class VersionImpl implements Version{
	/**
	 * 引用计数，当前version被引用的次数
	 */
	private final AtomicInteger ref = new AtomicInteger(0);
	/**
	 * version所属versionSet
	 */
	private final VersionSet versionSet;
	/**
	 * 第0层
	 */
	private final Level0Impl level0;
	/**
	 * 存放level > 1的层次，按升序排列
	 */
	private final SortedMap<Integer, LevelImpl> levels;
	/**
	 * 需要进行合并的level
	 */
	private int compactionLevel;
	/**
	 * 需要合并的紧迫程度。当score>=1时，必须进行合并
	 */
	private double compactionScore;
	
	public VersionImpl(VersionSet versionSet) {
		this.versionSet = versionSet;
		this.level0 = new Level0Impl(versionSet.getTableCache());
		this.levels = new TreeMap<>();
		for(int i = 1; i < VersionSet.MAX_LEVELS; i++) {
			// 新建level
			LevelImpl leveln = new LevelImpl(i, versionSet.getTableCache());
			// 存入levels
			levels.put(i, leveln);
		}
	}
	@Override
	public LookupResult get(LookupKey key) throws Exception {
		// 先查找level0
		LookupResult result = level0.get(key);
		// 如果没有，再查找所有level
		if(result != null) {
			return result;
		}
		else {
			// 按Level层级升序查找
			for(Iterator<Entry<Integer, LevelImpl>> it = levels.entrySet().iterator(); it.hasNext(); ) {
				Level level = it.next().getValue();
				result = level.get(key);
				// 如果查询结果不为空，则返回查询结果
				if(result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	@Override
	public List<FileMetaData> getOverlappingInputs(int level, InternalKey smallest, InternalKey largest) {
		Preconditions.checkNotNull(smallest);
		Preconditions.checkNotNull(largest);
		Preconditions.checkNotNull(smallest.getUserKey());
		Preconditions.checkNotNull(largest.getUserKey());
		ImmutableList.Builder<FileMetaData> files = ImmutableList.builder();
		// 遍历level0层文件，寻找和上述范围有重叠的文件
		for (FileMetaData fileMetaData : getFiles(level)) {
			if(!(fileMetaData.getLargest().getUserKey().compareTo(smallest.getUserKey()) < 0
					|| fileMetaData.getSmallest().getUserKey().compareTo(largest.getUserKey()) > 0)) {
				files.add(fileMetaData);
			}
		}
		return files.build();
	}
	
	@Override
	public int refs() {
		return ref.get();
	}
	@Override
	public void retain() {
		ref.incrementAndGet();
	}
	@Override
	public void release() {
		ref.decrementAndGet();
	}
	@Override
	public int files(int level) {
		Preconditions.checkArgument(level - 1 < levels.size());
		return level == 0 ? level0.getFiles().size() : levels.get(level).getFiles().size();
	}
	@Override
	public double getCompactionScore() {
		return compactionScore;
	}
	@Override
	public void setCompactionScore(double score) {
		this.compactionScore = score;
	}
	@Override
	public int getCompactionLevel() {
		return compactionLevel;
	}
	@Override
	public void setCompactionLevel(int level) {
		this.compactionLevel = level;
	}
	@Override
	public List<FileMetaData> getFiles(int level) {
		Preconditions.checkArgument(level < VersionSet.MAX_LEVELS && level >= 0);
		if(level == 0) {
			return level0.getFiles();
		}
		else {
			return levels.get(level).getFiles();
		}
	}
	@Override
	public void addFile(int level, FileMetaData fileMetaData) {
		Preconditions.checkArgument(level < VersionSet.MAX_LEVELS && level >= 0);
		if(level == 0) {
			level0.addFile(fileMetaData);
		}
		else {
			LevelImpl leveln = levels.get(level);
			leveln.addFile(fileMetaData);
		}
		
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String ret = "\n*****************Version: " + "\nlevel0=" + level0;
		sb.append(ret);
		levels.forEach((level, levelImpl) -> {
			sb.append("\n" + level + " : " + levelImpl.toString());
		});
		sb.append(", \ncompactionLevel=" + compactionLevel + ", \ncompactionScore=" + compactionScore + "\n*****************");
		return sb.toString();
	}

}
