package lsm;

import java.io.Closeable;
import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * 用于读取日志数据
 * @author bird
 *
 */
public interface LogReader extends Closeable{
	/**
	 * 顺序获取下一个位置的record
	 * @return record数据，如果数据读完则返回null
	 * @throws IOException 
	 */
	public ByteBuf readNextRecord() throws IOException;
}
