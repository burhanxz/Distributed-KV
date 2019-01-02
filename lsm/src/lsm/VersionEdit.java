package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.FileMetaData;

public interface VersionEdit {
	public ByteBuf encode();
	
	/**
	 * 设置被删除的文件信息
	 * @param level 被删文件所属level
	 * @param fileNumber 被删文件编号
	 */
	public void deleteFile(int level, long fileNumber);
	
	/**
	 * 设置增加的文件信息 
	 * @param level 增加文件所属level
	 * @param fileMetaData 增加文件的信息
	 */
	public void addFile(int level, FileMetaData fileMetaData);
}
