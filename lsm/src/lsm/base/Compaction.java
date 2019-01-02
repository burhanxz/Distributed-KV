package lsm.base;

import java.util.List;

import lsm.Version;
import lsm.VersionEdit;

public class Compaction {
	private final Version inputVersion;
	private final VersionEdit edit;
	private final int level;
	
	private final List<FileMetaData> levelInputs;
	private final List<FileMetaData> levelUpInputs;
	
	private final long maxOutputFileSize;

	public Compaction(Version inputVersion, int level, List<FileMetaData> levelInputs,
			List<FileMetaData> levelUpInputs, long maxOutputFileSize) {
		this.inputVersion = inputVersion;
		this.level = level;
		this.levelInputs = levelInputs;
		this.levelUpInputs = levelUpInputs;
		this.maxOutputFileSize = maxOutputFileSize;
		// TODO
		this.edit = null;
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
	
}
