package lsm;

import java.io.Closeable;
import java.io.IOException;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

/**
 * 用于日志写入
 * k-v日志记录的格式为:
 * | record sum 总大小(4B) | internalKey size (4B) | internalKey(见InternalKey序列化结构) | value size (4B) | value |
 * @author bird
 *
 */
public interface LogWriter extends Closeable{
	/**
	 * log文件 以32KB为单位存取
	 */
	public final static int LOG_BLOCK_SIZE = 1 << 15;
	/**
	 * 获取绑定log的文件编号
	 * @return
	 */
	public long getFileNumber();
	/**
	 * 将记录写入日志
	 * @param record 信息记录
	 * @param force 是否同步刷盘
	 * @throws IOException 
	 */
	public void addRecord(ByteBuf record, boolean force) throws IOException;
	/**
	 * 将键和值写入日志
	 * @param internalKey
	 * @param value
	 * @param force
	 * @throws IOException 
	 */
	public void addRecord(InternalKey internalKey, ByteBuf value, boolean force) throws IOException;
}
