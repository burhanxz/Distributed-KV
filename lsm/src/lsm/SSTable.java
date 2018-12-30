package lsm;

import java.io.Closeable;

import io.netty.buffer.ByteBuf;

public interface SSTable extends SeekingIterable<ByteBuf, ByteBuf>, Closeable{
	
}
