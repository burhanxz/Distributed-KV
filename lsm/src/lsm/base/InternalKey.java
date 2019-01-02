package lsm.base;

import java.util.Map;

import io.netty.buffer.ByteBuf;

public class InternalKey implements Map.Entry<ByteBuf, Long>{
	
	private ByteBuf userKey;
	private Long seq;
	
	public InternalKey(ByteBuf userKey, long seq) {
		this.userKey = userKey;
		this.seq = seq;
	}

	@Override
	public ByteBuf getKey() {
		return userKey;
	}

	@Override
	public Long getValue() {
		return seq;
	}

	@Override
	public Long setValue(Long value) {
		throw new UnsupportedOperationException();
	}

}
