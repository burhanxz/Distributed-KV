package lsm;

import io.netty.buffer.ByteBuf;

/**
 * meta block 的过滤策略
 * @author bird
 *
 */
public interface FilterPolicy {
	/**
	 * 根据keys创建filter数据
	 * @param keys 原始数据
	 * @return
	 */
	public ByteBuf createFilter(ByteBuf[] keys);
	/**
	 * 根据key和过滤数据来判断key是否存在
	 * @param key 键
	 * @param filter 过滤数据
 	 * @return 是否存在
	 */
	public boolean keyMayMatch(ByteBuf key, ByteBuf filter);
}
