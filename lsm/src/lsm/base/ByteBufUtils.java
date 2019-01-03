package lsm.base;

import io.netty.buffer.ByteBuf;

public class ByteBufUtils {
	public static void markIndex(ByteBuf... buffers) {
		for(ByteBuf buffer : buffers) {
			// 记录bytebuf信息
			buffer.markReaderIndex();
			buffer.markWriterIndex();
		}
	}
	
	public static void resetIndex(ByteBuf... buffers) {
		for(ByteBuf buffer : buffers) {
			// 恢复bytebuf信息
			buffer.resetReaderIndex();
			buffer.resetWriterIndex();
		}
	}
}
