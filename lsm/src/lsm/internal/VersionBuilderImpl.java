package lsm.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import lsm.Version;
import lsm.VersionBuilder;
import lsm.VersionSet;
import lsm.base.FileMetaData;
import lsm.base.Options;

public class VersionBuilderImpl implements VersionBuilder{
	private VersionSet versionSet;
	private Version base;
	private Map<Integer, Set<Long>> deletedFiles;
	private Map<Integer, SortedSet<FileMetaData>> newFiles;
	public VersionBuilderImpl(VersionSet versionSet, Version base) {
		this.versionSet = versionSet;
		this.base = base;
		deletedFiles = new HashMap<>();
		newFiles = new HashMap<>();
	}
	@Override
	public void apply(VersionEdit edit) {
		// 更新version set的compact pointers
		versionSet.setCompactPointers(edit.getCompactPointers());
		// 更新delete files信息
		edit.getDeletedFiles().forEach((level, fileNumbers) -> {
			if(deletedFiles.get(level) == null) {
				deletedFiles.put(level, new HashSet<>());
			}
			deletedFiles.get(level).addAll(fileNumbers);
		});
		//更新new files信息
		edit.getNewFiles().forEach((level, fileMetaDatas) -> {
			if(newFiles.get(level) == null) {
				newFiles.put(level, new TreeSet<>());
			}
			newFiles.get(level).addAll(fileMetaDatas);
			// 同时，在deleted files中删除new files中已存在的文件
			fileMetaDatas.forEach(fileMetaData -> {
				if(deletedFiles.containsKey(level)) {
					deletedFiles.get(level).remove(fileMetaData.getNumber());
				}
			});
		});
	}

	@Override
	public Version build() {
		// TODO
		Version version = null;
		// 定义文件排序的比较器
		Comparator<FileMetaData> comparator = new Comparator<FileMetaData>() {
			@Override
			public int compare(FileMetaData left, FileMetaData right) {
				// 先按文件中最小值排序，再按文件编号排序
				return ComparisonChain.start().compare(left.getSmallest(), right.getSmallest(), Options.INTERNAL_KEY_COMPARATOR)
						.compare(left.getNumber(), right.getNumber()).result();
			}};
		// 逐层添加文件，要求0层以上文件不能重叠
		for(int i = 0; i <= base.maxLevel(); i++) {
			// 将base version和新添加文件全部加入到待排序列表中
			List<FileMetaData> baseFiles = base.getFiles(i);
			SortedSet<FileMetaData> addedFiles = newFiles.get(i);
			ArrayList<FileMetaData> sortedFiles = new ArrayList<>(baseFiles.size() + addedFiles.size());
			sortedFiles.addAll(baseFiles);
			sortedFiles.addAll(addedFiles);
			// 按照文件最小值和编号排序
			Collections.sort(sortedFiles, comparator);
			final int level = i;
			// 考虑是否将这些文件加入到新建的version中
			sortedFiles.forEach(fileMetaData -> {
				// 如果在deleted files中不包含，则考虑下一步
				if(!(deletedFiles.containsKey(level) && deletedFiles.get(level).contains(fileMetaData))) {
					List<FileMetaData> files = version.getFiles(level);
					if(level > 0 && !files.isEmpty()) {
						// TODO 查看本层所有文件是否与选中文件有重叠
						boolean overlap = Options.INTERNAL_KEY_COMPARATOR.compare(files.get(files.size() - 1).getLargest(), fileMetaData.getSmallest()) >= 0;
						Preconditions.checkState(!overlap);
					}
					version.addFile(level, fileMetaData);
				}
			});
		}
		return version;
	}


	
}
