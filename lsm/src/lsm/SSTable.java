package lsm;

import java.io.Closeable;

import io.netty.buffer.ByteBuf;

/**
 * 代表SSTable实体，可由此访问sstable中的内容
 * @author bird
 *
 */
public interface SSTable extends SeekingIterable<ByteBuf, ByteBuf>, Closeable{
	public Block openBlock(int blockOffset, int blockSize);
}
