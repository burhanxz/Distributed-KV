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
import java.util.stream.IntStream;

import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;

import lsm.Version;
import lsm.VersionBuilder;
import lsm.VersionSet;
import lsm.base.FileMetaData;
import lsm.base.Options;

public class VersionBuilderImpl implements VersionBuilder{
	/**
	 * 定义文件排序的比较器
	 */
	private static final Comparator<FileMetaData> comparator = new Comparator<FileMetaData>() {
		@Override
		public int compare(FileMetaData left, FileMetaData right) {
			// 先按文件中最小值排序，再按文件编号排序
			return ComparisonChain.start().compare(left.getSmallest(), right.getSmallest(), Options.INTERNAL_KEY_COMPARATOR)
					.compare(left.getNumber(), right.getNumber()).result();
		}};
	/**
	 * version builder 所属version set
	 */
	private VersionSet versionSet;
	/**
	 * version builder 所使用的base, base + edit = a new version
	 */
	private Version base;
	/**
	 * 标记为需要删除的文件
	 */
	private Map<Integer, Set<Long>> deletedFiles;
	/**
	 * 标记为需要添加的文件
	 */
	private Map<Integer, SortedSet<FileMetaData>> newFiles;
	public VersionBuilderImpl(VersionSet versionSet, Version base) {
		this.versionSet = versionSet;
		this.base = base;
		deletedFiles = new HashMap<>();
		newFiles = new HashMap<>();
		IntStream.range(0, VersionSet.MAX_LEVELS).forEach(level -> {
			newFiles.put(level, new TreeSet<>(comparator));
			deletedFiles.put(level, new HashSet<>());
		});
	}
	@Override
	public void apply(VersionEdit edit) {
		// 更新version set的compact pointers
		versionSet.setCompactPointers(edit.getCompactPointers());
		// 更新delete files信息
		edit.getDeletedFiles().forEach((level, fileNumbers) -> {
			deletedFiles.get(level).addAll(fileNumbers);
		});
		//更新new files信息
		edit.getNewFiles().forEach((level, fileMetaDatas) -> {
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
		// 新建version
		Version version = new VersionImpl(versionSet);

		// 逐层添加文件，要求0层以上文件不能重叠
		for(int i = 0; i < VersionSet.MAX_LEVELS; i++) {
			// 将base version和新添加文件全部加入到待排序列表中
			List<FileMetaData> baseFiles = base.getFiles(i);
			SortedSet<FileMetaData> addedFiles = newFiles.get(i);
			ArrayList<FileMetaData> sortedFiles = new ArrayList<>();
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
						// 查看本层所有文件是否与选中文件有重叠
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
