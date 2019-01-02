package lsm.internal;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

public class SSTableBuilder {
	/**
	 * 加入k-v对到sstable中
	 * @param entry
	 */
	public void add(Entry<InternalKey, ByteBuf> entry) {
		add(entry.getKey().encode(), entry.getValue());
	}
	
	public void add(ByteBuf key, ByteBuf value) {
		//TODO
	}
	
	public void finish() {
		//TODO
	}
}
