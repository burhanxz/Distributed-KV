package lsm;

import io.netty.buffer.ByteBuf;

public interface BlockBuilder {
	public void add(ByteBuf key, ByteBuf value);
	/**
	 * 结束block builder，写入重启点位置和数量信息
	 * @return 返回字节数据
	 */
	public ByteBuf finish();
	/**
	 * block builder中record条数
	 * @return
	 */
	public int count();
	/**
	 * block实际大小
	 * @return
	 */
	public int size();
}
