package lsm.base;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.io.File;
import java.io.IOException;

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
		File file = new File(databaseDir, fileName);
		if(!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
	
	public static String sstableFileName(long fileNumber) {
		return makeFileName(fileNumber, ".sst");
	}
	
    private static String makeFileName(long number, String suffix)
    {
        return String.format("%06d.%s", number, suffix);
    }
}
