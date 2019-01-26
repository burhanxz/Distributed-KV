package lsm.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.collect.Lists;

import lsm.Level;
import lsm.TableCache;
import lsm.base.FileMetaData;

/**
 * Level的通用实现
 * @author bird
 *
 */
public abstract class AbstractLevel implements Level{
	/**
	 * 层级
	 */
	protected final int level;
	/**
	 * 本层所含文件信息
	 */
	protected SortedMap<FileMetaData, Long> files;
	protected TableCache cache;
	public AbstractLevel(int level, TableCache cache) {
		this.level = level;
		this.cache = cache;
	}

	@Override
	public List<FileMetaData> getFiles() {
		return Lists.newArrayList(files.keySet());
	}

	@Override
	public void addFile(FileMetaData file) {
		files.put(file, file.getNumber());	
	}

	@Override
	public int getLevelNumber() {
		return level;
	}
	

}
