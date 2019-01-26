package lsm.internal;

import java.util.Comparator;
import java.util.TreeMap;

import lsm.TableCache;
import lsm.base.FileMetaData;
import lsm.base.LookupKey;
import lsm.base.LookupResult;
import lsm.base.Options;

public class Level0Impl extends AbstractLevel{
	public Level0Impl(int level, TableCache cache) {
		super(level, cache);
		// 按照file的新旧程度进行排序，即按照fileNumber大小进行降序排序
		files = new TreeMap<>(new Comparator<FileMetaData>() {
			@Override
			public int compare(FileMetaData left, FileMetaData right) {
				return -(int) (left.getNumber() - right.getNumber());
			}
		});
	}
	@Override
	public LookupResult get(LookupKey key) throws Exception {
		// 获取第一个大小范围包含key的文件
		long fileNumber = 0;
		long fileSize = 0;
		for(FileMetaData file : files.keySet()) {
			if(Options.INTERNAL_KEY_COMPARATOR.compare(file.getLargest(), key.getInternalKey()) > 0
					&& Options.INTERNAL_KEY_COMPARATOR.compare(file.getSmallest(), key.getInternalKey()) < 0) {
				fileNumber = files.get(file);
				fileSize = file.getFileSize();
				break;
			}
		}
		// 如果没有满足要求的文件，返回null
		if(fileNumber <= 0) {
			return null;
		}
		// 通过这个文件去tableCache中查找key
		return cache.get(key, fileNumber, fileSize);
	}

}
