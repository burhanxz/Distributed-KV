package lsm;

import io.netty.buffer.ByteBuf;

public interface LogWriter {
	/**
	 * 获取绑定log的文件编号
	 * @return
	 */
	public long getFileNumber();
	/**
	 * 将记录写入日志
	 * @param record 信息记录
	 * @param force 是否同步刷盘
	 */
	public void addRecord(ByteBuf record, boolean force);
}
