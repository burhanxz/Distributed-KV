package lsm.base;

import io.netty.buffer.ByteBuf;

/**
 * 查询内容
 * @author bird
 *
 */
public class LookupKey {
	/**
	 * 持有唯一的internalKey作为查找数据
	 */
	private final InternalKey internalKey;
	public LookupKey(InternalKey internalKey) {
		this.internalKey = internalKey;
	}
	public InternalKey getInternalKey() {
		return internalKey;
	}
	public ByteBuf getUserKey() {
		return internalKey.getUserKey();
	}
}
