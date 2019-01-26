package lsm.base;
import java.io.File;
import java.io.IOException;

import com.google.common.base.Preconditions;

/**
 * LSM文件工具类，负责创建各种文件以及获取各种文件名
 * @author bird
 *
 */
public class FileUtils {
	/**
	 * 新建sstable文件
	 * @param databaseDir
	 * @param fileNumber
	 * @return
	 * @throws IOException
	 */
	public static File newSSTableFile(File databaseDir, long fileNumber) throws IOException {
		String fileName = sstableFileName(fileNumber);
		return makeFile(databaseDir, fileName);
	}
	
	/**
	 * 新建log文件或manifest文件
	 * @param databaseDir
	 * @param fileNumber
	 * @param isManifest 是否是manifest文件
	 * @return
	 * @throws IOException
	 */
	public static File newLogFile(File databaseDir, long fileNumber, boolean isManifest) throws IOException {
		String fileName = isManifest ? manifestFileName(fileNumber) : logFileName(fileNumber);
		return makeFile(databaseDir, fileName);
	}
	/**
	 * 根据目录和文件名创建文件
	 * @param databaseDir
	 * @param fileName
	 * @return
	 * @throws IOException
	 */
	private static File makeFile(File databaseDir, String fileName) throws IOException {
		Preconditions.checkNotNull(databaseDir);
		Preconditions.checkState(databaseDir.exists() && databaseDir.isDirectory());
		Preconditions.checkNotNull(fileName);
		Preconditions.checkState(!fileName.isEmpty());
		File file = new File(databaseDir, fileName);
		if(!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	public static String sstableFileName(long fileNumber) {
		return makeFileName(fileNumber, ".sst");
	}
	
	public static String manifestFileName(long fileNumber) {
		return makeFileName(fileNumber, ".manifest");
	}

	public static String tempFileName(long fileNumber) {
		return makeFileName(fileNumber, ".tmp");
	}
	
	public static String logFileName(long fileNumber) {
		return makeFileName(fileNumber, ".log");
	}

	public static String currentFileName() {
		return "CURRENT";
	}
	
    private static String makeFileName(long number, String suffix)
    {
        return String.format("%06d%s", number, suffix);
    }
}
