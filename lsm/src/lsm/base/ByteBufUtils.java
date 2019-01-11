package lsm.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufUtils {
		
	public static void write(FileChannel channel, ByteBuf buffer) throws IOException {
		// 建立bytebuffer以便写入文件
		ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.readableBytes());
		// 将bytebuf中的数据导入bytebuffer中
		buffer.slice().readBytes(tmpBuffer);
		// bytebuffer数据写入fileChannel
		tmpBuffer.flip();
		channel.write(tmpBuffer);
	}
	public static void read(FileChannel channel, ByteBuf buffer) throws IOException {
		// 建立bytebuffer以便写入文件
		ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.readableBytes());
		// 文件数据写入buffer
		channel.read(tmpBuffer);
		tmpBuffer.flip();
		// bytebuffer写入byteBuf
		buffer.writeBytes(tmpBuffer);
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
	public static String buf2Str(ByteBuf buf) {
	    String str;
	    // 处理堆缓冲区
	    if(buf.hasArray()) {
	        str = new String(buf.array(), buf.arrayOffset() + buf.readerIndex(), buf.readableBytes());
	    } else { 
	    	// 处理直接缓冲区以及复合缓冲区
	        byte[] bytes = new byte[buf.readableBytes()];
	        buf.getBytes(buf.readerIndex(), bytes);
	        str = new String(bytes, 0, buf.readableBytes());
	    }
	    return str;
	}

}
