package lsm.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;

public class ByteBufUtils {
	public static void write(FileChannel channel, ByteBuf buffer) throws IOException {
		// 建立bytebuffer以便写入文件
		ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.readableBytes());
		// 将bytebuf中的数据导入bytebuffer中
		buffer.readBytes(tmpBuffer);
		// bytebuffer数据写入fileChannel
		tmpBuffer.flip();
		channel.write(tmpBuffer);
	}
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
