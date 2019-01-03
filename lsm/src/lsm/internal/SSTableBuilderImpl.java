package lsm.internal;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.SSTableBuilder;
import lsm.base.InternalKey;

public class SSTableBuilderImpl implements SSTableBuilder {

	@Override
	public void add(Entry<InternalKey, ByteBuf> entry) {
		add(entry.getKey().encode(), entry.getValue());
	}

	@Override
	public void add(ByteBuf key, ByteBuf value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void abandon() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getFileSize() {
		// TODO Auto-generated method stub
		return 0;
	}

}
