package lsm.internal;

import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.netty.buffer.ByteBuf;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;

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
	private final Multimap<Integer, FileMetaData> newFiles = ArrayListMultimap.create();
	private final Multimap<Integer, Long> deletedFiles = ArrayListMultimap.create();
	/**
	 * 将versionEdit编码为字节，方便序列化
	 * @return 字节数据
	 */
	public ByteBuf encode() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * 设置被删除的文件信息
	 * @param level 被删文件所属level
	 * @param fileNumber 被删文件编号
	 */
	public void deleteFile(int level, long fileNumber) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * 设置增加的文件信息 
	 * @param level 增加文件所属level
	 * @param fileMetaData 增加文件的信息
	 */
	public void addFile(int level, FileMetaData fileMetaData) {
		// TODO Auto-generated method stub
		
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

}
