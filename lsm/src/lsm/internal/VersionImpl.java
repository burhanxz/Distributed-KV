package lsm.internal;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.Level;

import lsm.Version;
import lsm.VersionSet;
import lsm.base.FileMetaData;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

public class VersionImpl implements Version{
	/**
	 * 引用计数，当前version被引用的次数
	 */
	private final AtomicInteger ref = new AtomicInteger(0);
	private final VersionSet versionSet;
	private final Level level0;
	private final List<Level> levels;

	// 需要进行合并的level
	private int compactionLevel;
	// 当score>=1时，也需要进行合并
	private double compactionScore;
	
	public VersionImpl(VersionSet versionSet) {
		this.versionSet = versionSet;
		//TODO
		this.level0 = null;
		this.levels = null;
	}
	@Override
	public LookupResult get(LookupKey key) throws Exception {
		// 增加引用计数
//		retain();
		// 先查找level0
		LookupResult result = level0.get(key);
		// 如果没有，再查找所有level
		if(result != null) {
			// 减少引用计数
			release();
			return result;
		}
		else {
			for(Level level : levels) {
				result = level.get(key);
				if(result != null) {
					// 减少引用计数
					release();
					return result;
				}
			}
		}
		// 减少引用计数
//		release();
		return null;
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
	public int maxLevel() {
		return levels.size();
	}
	@Override
	public int files(int level) {
		Preconditions.checkArgument(level - 1 < levels.size());
		return level == 0 ? level0.getFiles().size() : levels.get(level - 1).getFiles().size();
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
		Preconditions.checkArgument(level - 1 < levels.size());
		if(level == 0) {
			return level0.getFiles();
		}
		else {
			return levels.get(level - 1).getFiles();
		}
	}
	@Override
	public void addFile(int level, FileMetaData fileMetaData) {
		Preconditions.checkArgument(level - 1 < levels.size());
		if(level == 0) {
			level0.addFile(fileMetaData);
		}
		else {
			levels.get(level - 1).addFile(fileMetaData);
		}
		
	}
}
