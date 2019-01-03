package lsm;

import io.netty.buffer.ByteBuf;

public interface SSTableBlockBuilder {
	public void add(ByteBuf key, ByteBuf value);
}
