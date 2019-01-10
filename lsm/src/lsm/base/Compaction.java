package lsm.base;

import java.util.List;

import lsm.Version;
import lsm.internal.VersionEdit;

public class Compaction {
	private final Version inputVersion;
	private final VersionEdit edit;
	private final int level;
	
	private final List<FileMetaData> levelInputs;
	private final List<FileMetaData> levelUpInputs;
	
	private final long maxOutputFileSize;

	public Compaction(Version inputVersion, int level, List<FileMetaData> levelInputs,
			List<FileMetaData> levelUpInputs) {
		this.inputVersion = inputVersion;
		this.level = level;
		this.levelInputs = levelInputs;
		this.levelUpInputs = levelUpInputs;
		
		// TODO
		this.maxOutputFileSize = 0;
		this.edit = null;
	}
	
	public void addInputDeletions(VersionEdit edit) {
		//TODO
	}
	
	public boolean isMinorCompaction() {
		// TODO
		return false;
	}

	/**
	 * 获取低一层需要compact的文件
	 * @return
	 */
	public List<FileMetaData> getLevelInputs() {
		return levelInputs;
	}

	/**
	 * 获取所属edit
	 * @return
	 */
	public VersionEdit getEdit() {
		return edit;
	}

	/**
	 * 获取需要compact的低一层
	 * @return
	 */
	public int getLevel() {
		return level;
	}

	public List<FileMetaData> getLevelUpInputs() {
		return levelUpInputs;
	}

	public long getMaxOutputFileSize() {
		return maxOutputFileSize;
	}
	
}
