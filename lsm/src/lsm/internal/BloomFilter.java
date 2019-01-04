package lsm.internal;

import io.netty.buffer.ByteBuf;
import lsm.FilterPolicy;

public class BloomFilter implements FilterPolicy{

	@Override
	public ByteBuf createFilter(ByteBuf[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean keyMayMatch(ByteBuf key, ByteBuf filter) {
		// TODO Auto-generated method stub
		return false;
	}

}
