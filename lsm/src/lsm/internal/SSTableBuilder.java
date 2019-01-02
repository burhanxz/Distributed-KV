package lsm.internal;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;

public class SSTableBuilder {
	
	/**
	 * 加入k-v对到sstable中
	 * @param entry
	 */
	public void add(Entry<ByteBuf, ByteBuf> entry) {
		add(entry.getKey(), entry.getValue());
	}
	
	public void add(ByteBuf key, ByteBuf value) {
		//TODO
	}
}
