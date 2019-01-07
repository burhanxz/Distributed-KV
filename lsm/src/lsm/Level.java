package lsm;

import java.util.List;

import io.netty.buffer.ByteBuf;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

/**
 * 代表sstable分层存储中的一个层次
 * @author bird
 */
public interface Level{
	/**
	 * 在当前level的所有文件里查找key
	 * @param key 查找的key
	 * @return 查找结果
	 */
	public LookupResult get(LookupKey key);
	/**
	 * 获取本层所有的文件
	 * @return
	 */
	public List<FileMetaData> getFiles();
	/**
	 * 加入文件
	 * @param file
	 */
	public void addFile(FileMetaData file);
	/**
	 * 获取本层的层号
	 * @return
	 */
	public int getLevelNumber();
}
