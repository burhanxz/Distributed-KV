package lsm.base;

import java.util.List;

import com.google.common.base.Preconditions;

import lsm.Version;
import lsm.VersionSet;
import lsm.internal.VersionEdit;
import lsm.internal.VersionSetImpl;

/**
 * 存有compact信息
 * @author bird
 *
 */
public class Compaction {
	public final static long MAX_FILE_SIZE = 1 << 21;
	private final Version inputVersion;
	/**
	 * 本次compaction所属edit
	 */
	private final VersionEdit edit;
	/**
	 * 待compact的层级
	 */
	private final int level;
	/**
	 * level层待compact文件
	 */
	private final List<FileMetaData> levelInputs;
	/**
	 * level + 1层待compact文件
	 */
	private final List<FileMetaData> levelUpInputs;
	
	public Compaction(Version inputVersion, int level, List<FileMetaData> levelInputs,
			List<FileMetaData> levelUpInputs) {
		this.inputVersion = inputVersion;
		this.level = level;
		this.levelInputs = levelInputs;
		this.levelUpInputs = levelUpInputs;
		this.edit = new VersionEdit();
	}
	
	/**
	 * 把compaction引用的文件，在edit中标记为删除
	 * @param edit
	 */
	public void deleteFiles(VersionEdit edit) {
		Preconditions.checkNotNull(edit);
		// 遍历标记level层
		levelInputs.forEach(file -> {
			edit.deleteFile(level, file.getNumber());
		});
		// 遍历标记level + 1层
		levelUpInputs.forEach(file -> {
			edit.deleteFile(level + 1, file.getNumber());
		});
	}
	
	public boolean isMinorCompaction() {
		// 如果低一层只有一个待compact文件，高一层无compact文件，则进行minor compaction
		boolean isMinorCompaction = levelInputs.size() == 1 && levelUpInputs.isEmpty();
		return isMinorCompaction;
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
