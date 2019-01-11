package lsm.internal;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;

/**
 * versionEdit在manifest文件中的序列化结构:
 * | comparator name | log number(8B) | next file number(8B) | last seq(8B) |
 * | compact pointer | delete file | new file |
 * 其中：
 * comparator name结构:
 * | name string length(4B) | name string |
 * compact pointer结构(多个):
 * | level(4B) | Internal key 长度 | InternalKey(见InternalKey序列化结构) |
 * delete file结构：
 * | level(4B) | long列表长度(4B) | long(8B)... |
 * new file结构(多个):
 * | level(4B) | fileMetaData 长度 | fileMetaData(见fileMetaData序列化结构) |
 * @author bird
 *
 */
public class VersionEdit{
	/**
	 * key comparator名字  
	 */
	private String comparatorName; 
	/**
	 * 日志编号
	 */
	private Long logNumber;
	/**
	 * 下一个文件编号
	 */
	private Long nextFileNumber;
	/**
	 * db中最大的seq（序列号），即最后一对kv事务操作的序列号
	 */
	private Long lastSequenceNumber;
	
	private final Map<Integer, InternalKey> compactPointers = new TreeMap<>();
	private final Map<Integer, List<Long>> deletedFiles = new HashMap<>();
	private final Map<Integer, List<FileMetaData>> newFiles = new HashMap<>();
	/**
	 * 将versionEdit编码为字节，方便序列化.序列化结构参考VersionEdit doc
	 * @return 字节数据
	 */
	public ByteBuf encode() {
		Preconditions.checkNotNull(comparatorName);
		Preconditions.checkArgument(logNumber >= 0);
		Preconditions.checkArgument(nextFileNumber >= 0);
		Preconditions.checkArgument(lastSequenceNumber >= 0);
		ByteBuf buff = PooledByteBufAllocator.DEFAULT.buffer();
		// 序列化comparator name
		byte[] comparator = comparatorName.getBytes(StandardCharsets.UTF_8);
		buff.writeInt(comparator.length);
		buff.writeBytes(comparator);
		// 序列化log number
		buff.writeLong(logNumber);
		// 序列化next file number
		buff.writeLong(nextFileNumber);
		// 序列化last seq
		buff.writeLong(lastSequenceNumber);
		// 序列化compact pointers
		compactPointers.forEach((level, key) -> {
			buff.writeInt(level);
			ByteBuf keyBuffer = key.encode();
			buff.writeInt(keyBuffer.readableBytes());
			buff.writeBytes(keyBuffer);
		});
		// 序列化delete files
		deletedFiles.forEach((level, fileNumbers) -> {
			buff.writeInt(level);
			buff.writeInt(fileNumbers.size());
			fileNumbers.forEach(fileNumber -> {
				buff.writeLong(fileNumber);
			});
		});
		// 序列化new files
		newFiles.forEach((level, fileMetaData) -> {
			buff.writeInt(level);
			//TODO
//			ByteBuf fileMetaDataBuffer = fileMetaData.encode();
//			buff.writeInt(fileMetaDataBuffer.readableBytes());
//			buff.writeBytes(fileMetaDataBuffer);
		});
		return buff;
	}
	/**
	 * 设置被删除的文件信息
	 * @param level 被删文件所属level
	 * @param fileNumber 被删文件编号
	 */
	public void deleteFile(int level, long fileNumber) {
		Preconditions.checkArgument(level >= 0);
		Preconditions.checkArgument(fileNumber >= 0);
		if(deletedFiles.get(level) == null) {
			deletedFiles.put(level, new ArrayList<>());
		}
		deletedFiles.get(level).add(fileNumber);
	}
	/**
	 * 设置增加的文件信息 
	 * @param level 增加文件所属level
	 * @param fileMetaData 增加文件的信息
	 */
	public void addFile(int level, FileMetaData fileMetaData) {
		Preconditions.checkArgument(level >= 0);
		Preconditions.checkNotNull(fileMetaData);
		if(newFiles.get(level) == null) {
			newFiles.put(level, new ArrayList<>());
		}
		newFiles.get(level).add(fileMetaData);
	}
	/**
	 * 一组file合并到newFiles中
	 * @param files
	 */
	public void addFiles(Map<Integer, List<FileMetaData>> files) {
		Preconditions.checkNotNull(files);
		// 将newFiles和参数中files合并
		files.forEach((level, list) -> {
			if(newFiles.get(level) == null) {
				newFiles.put(level, list);
			}
			else {
				newFiles.get(level).addAll(list);
			}
		});
	}
	//getter and setter
	public void setComparatorName(String comparatorName) {
		this.comparatorName = comparatorName;
	}
	public void setLogNumber(Long logNumber) {
		this.logNumber = logNumber;
	}
	public void setNextFileNumber(Long nextFileNumber) {
		this.nextFileNumber = nextFileNumber;
	}
	public void setLastSequenceNumber(Long lastSequenceNumber) {
		this.lastSequenceNumber = lastSequenceNumber;
	}
	public String getComparatorName() {
		return comparatorName;
	}
	public Long getLogNumber() {
		return logNumber;
	}
	public Long getNextFileNumber() {
		return nextFileNumber;
	}
	public Long getLastSequenceNumber() {
		return lastSequenceNumber;
	}
	public void setCompactPointer(int level, InternalKey levelLargest) {
		compactPointers.put(level, levelLargest);
	}
	public void setCompactPointers(Map<Integer, InternalKey> compactPointersMap) {
		compactPointers.putAll(compactPointersMap);
	}
	public Map<Integer, InternalKey> getCompactPointers() {
		return compactPointers;
	}
	public Map<Integer, List<Long>> getDeletedFiles() {
		return deletedFiles;
	}
	public Map<Integer, List<FileMetaData>> getNewFiles() {
		return newFiles;
	}


}
