package lsm;

import java.io.Closeable;
import java.io.IOException;

import io.netty.buffer.ByteBuf;

/**
 * 代表SSTable实体，可由此访问sstable中的内容
 * @author bird
 *
 */
public interface SSTable extends SeekingIterable<ByteBuf, ByteBuf>, Closeable{
	/**
	 * 打开文件上指定位置数据所代表的Block
	 * @param blockOffset 
	 * @param blockSize
	 * @return
	 * @throws IOException
	 */
	public Block openBlock(int blockOffset, int blockSize) throws IOException;
}
