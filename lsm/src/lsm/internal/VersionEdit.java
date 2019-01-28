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
import lsm.LogWriter;
import lsm.base.ByteBufUtils;
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
	private static final byte K_COMPARATOR = 1;
	private static final byte K_LOG_NUMBER = 2;
	private static final byte K_NEXT_FILE_NUMBER = 3;
	private static final byte K_LAST_SEQ = 4;
	private static final byte K_COMPACT_POINT = 5;
	private static final byte K_DELETE_FILE_NO = 6;
	private static final byte K_NEW_FILE_NO = 7;
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
	
	private Map<Integer, InternalKey> compactPointers;
	private Map<Integer, List<Long>> deletedFiles;
	private Map<Integer, List<FileMetaData>> newFiles;
	public VersionEdit() {
		comparatorName = null;
		logNumber = -1L;
		nextFileNumber = -1L;
		lastSequenceNumber = -1L;
		compactPointers = new TreeMap<>();
		deletedFiles = new HashMap<>();
		newFiles = new HashMap<>();
	}
	/**
	 * 将versionEdit编码为字节，方便序列化.序列化结构参考VersionEdit doc
	 * @return 字节数据
	 */
	public ByteBuf encode() {
		// 分配bytebuf，容量为一个日志block
		ByteBuf dst = PooledByteBufAllocator.DEFAULT.buffer(LogWriter.LOG_BLOCK_SIZE);
		// 序列化比较器
		if(comparatorName != null) {
			ByteBufUtils.putVarByte(dst, K_COMPARATOR);
			ByteBufUtils.putVarWithLenPrefix(dst, comparatorName.getBytes());
		}
		// 序列化log number
		if(logNumber >= 0) {
			ByteBufUtils.putVarByte(dst, K_LOG_NUMBER);
			ByteBufUtils.putVarLong(dst, logNumber);
		}
		// 序列化next file number
		if(nextFileNumber >= 0) {
			ByteBufUtils.putVarByte(dst, K_NEXT_FILE_NUMBER);
			ByteBufUtils.putVarLong(dst, nextFileNumber);
		}
		// 序列化last seq
		if(lastSequenceNumber >= 0) {
			ByteBufUtils.putVarByte(dst, K_LAST_SEQ);
			ByteBufUtils.putVarLong(dst, lastSequenceNumber);
		}
		// 序列化compact pointer
		if(compactPointers != null) {
			compactPointers.forEach((level, internalKey) -> {
				ByteBufUtils.putVarByte(dst, K_COMPACT_POINT);
				ByteBufUtils.putVarInt(dst, level);
				ByteBufUtils.putVarWithLenPrefix(dst, internalKey.encode());
			});
		}
		// 序列化delete file
		if(deletedFiles != null) {
			deletedFiles.forEach((level, list) -> {
				list.forEach(fileNumber -> {
					ByteBufUtils.putVarByte(dst, K_DELETE_FILE_NO);
					ByteBufUtils.putVarInt(dst, level);
					ByteBufUtils.putVarLong(dst, fileNumber);
				});
			});
		}
		// 序列化new file
		if(newFiles != null) {
			newFiles.forEach((level, list) -> {
				list.forEach(fileMetaData -> {
					ByteBufUtils.putVarByte(dst, K_NEW_FILE_NO);
					ByteBufUtils.putVarInt(dst, level);
					ByteBufUtils.putVarWithLenPrefix(dst, fileMetaData.encode());
				});
			});
		}
		return dst;
	}
	public VersionEdit(ByteBuf record) {
		this();
		record = record.slice();
		// 按顺序读取record
		while(record.isReadable()) {
			// 获取tag数值，并据此解析数据
			byte tag = record.readByte();
			switch(tag) {
			// 解析comparator
			case K_COMPARATOR : {
				ByteBuf comparatorBytes = ByteBufUtils.getVarWithLenPrefix(record);
				comparatorName = ByteBufUtils.buf2Str(comparatorBytes);
				break;
			}
			// 解析log number
			case K_LOG_NUMBER :{
				logNumber = record.readLong();
				break;
			}
			// 解析next file number
			case K_NEXT_FILE_NUMBER:{
				nextFileNumber = record.readLong();
				break;
			}
			// 解析last seq
			case K_LAST_SEQ :{
				lastSequenceNumber = record.readLong();
				break;
			}
			// 解析 compact point
			case K_COMPACT_POINT :{
				int level = record.readInt();
				ByteBuf internalKeyBytes = ByteBufUtils.getVarWithLenPrefix(record);
				InternalKey internalKey = InternalKey.decode(internalKeyBytes);
				compactPointers.put(level, internalKey);
				break;
			}
			// 解析delete file number
			case K_DELETE_FILE_NO :{
				int level = record.readInt();
				long fileNumber = record.readLong();
				// 如果file number列表不存在，则新建列表
				List<Long> list = null;
				if((list = deletedFiles.get(level)) == null) {
					list = new ArrayList<>();
					deletedFiles.put(level, list);
				}
				list.add(fileNumber);
				break;
			}
			// 解析new file numbers
			case K_NEW_FILE_NO :{
				int level = record.readInt();
				ByteBuf fileMetaDataBytes = ByteBufUtils.getVarWithLenPrefix(record);
				FileMetaData fileMetaData = new FileMetaData(fileMetaDataBytes);
				// 如果new files 列表不存在则新建列表
				List<FileMetaData> list = null;
				if((list = newFiles.get(level)) == null) {
					list = new ArrayList<>();
					newFiles.put(level, list);
				}
				list.add(fileMetaData);
				break;
			}
			}
		}
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
