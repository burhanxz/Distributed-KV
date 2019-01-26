package lsm.base;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey.InternalKeyType;

/**
 * 查询结果
 * @author bird
 *
 */
public class LookupResult {
	/**
	 * 查询内容
	 */
	private final LookupKey key;
	/**
	 * 查询到的value
	 */
	private final ByteBuf value;
	/**
	 * 查询结果是否已经被标记为删除
	 */
	private final boolean isDeleted;
	public LookupResult(LookupKey key, ByteBuf value, InternalKeyType type) {
		this.key = key;
		this.value = value;
		this.isDeleted = (type == InternalKeyType.DELETE);
	}
	public LookupKey getKey() {
		return key;
	}
	public ByteBuf getValue() {
		return value;
	}
	public boolean isDeleted() {
		return isDeleted;
	}
}
