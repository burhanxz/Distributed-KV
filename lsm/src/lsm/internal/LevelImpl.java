package lsm.internal;

import java.util.Comparator;
import java.util.TreeMap;

import lsm.TableCache;
import lsm.base.FileMetaData;
import lsm.base.LookupKey;
import lsm.base.LookupResult;
import lsm.base.Options;

public class LevelImpl extends AbstractLevel{
	public LevelImpl(int level, TableCache cache) {
		super(level, cache);
		// 按照file的最大key进行升序排序
		files = new TreeMap<>(new Comparator<FileMetaData>() {
			@Override
			public int compare(FileMetaData left, FileMetaData right) {
				return Options.INTERNAL_KEY_COMPARATOR.compare(left.getLargest(), right.getLargest());
			}
		});
	}
	@Override
	public LookupResult get(LookupKey key) {
		// 获取第一个larget大于key的fileMetadata
		long fileNumber = 0;
		long fileSize = 0;
		for(FileMetaData file : files.keySet()) {
			if(Options.INTERNAL_KEY_COMPARATOR.compare(file.getLargest(), key.getKey()) > 0) {
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
