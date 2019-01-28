package lsm.base;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

/**
 * ByteBuf 工具类
 * @author bird
 *
 */
public class ByteBufUtils {
	/**
	 * 将buffer中的数据写入文件
	 * @param channel
	 * @param buffer
	 * @throws IOException
	 */
	public static void write(FileChannel channel, ByteBuf buffer) throws IOException {
		// 建立bytebuffer以便写入文件
		ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.readableBytes());
		// 将bytebuf中的数据导入bytebuffer中
		buffer.slice().readBytes(tmpBuffer);
		// bytebuffer数据写入fileChannel
		tmpBuffer.flip();
		channel.write(tmpBuffer);
	}
	/**
	 * 读取文件数据到ByteBuf中
	 * @param channel
	 * @param buffer
	 * @throws IOException
	 */
	public static void read(FileChannel channel, ByteBuf buffer) throws IOException {
		// 建立bytebuffer以便写入文件
		ByteBuffer tmpBuffer = ByteBuffer.allocate(buffer.capacity());
		// 文件数据写入buffer
		channel.read(tmpBuffer);
		tmpBuffer.flip();
		// bytebuffer写入byteBuf
		buffer.writeBytes(tmpBuffer);
	}
	public static void putVarByte(ByteBuf dst, byte data) {
		Preconditions.checkArgument(dst.writableBytes() >= Byte.BYTES);
		dst.writeByte(data);
	}
	public static void putVarInt(ByteBuf dst, int data) {
		Preconditions.checkArgument(dst.writableBytes() >= Integer.BYTES);
		dst.writeInt(data);
	}
	public static void putVarLong(ByteBuf dst, long data) {
		Preconditions.checkArgument(dst.writableBytes() >= Long.BYTES);
		dst.writeLong(data);
	}
	public static void putVarWithLenPrefix(ByteBuf dst, ByteBuf data) {
		Preconditions.checkArgument(dst.writableBytes() >= (data.readableBytes() + Integer.BYTES));
		dst.writeInt(data.readableBytes());
		dst.writeBytes(data.slice());
	}
	public static void putVarWithLenPrefix(ByteBuf dst, byte[] data) {
		Preconditions.checkArgument(dst.writableBytes() >= (data.length + Integer.BYTES));
		dst.writeInt(data.length);
		dst.writeBytes(data);
	}
	public static ByteBuf getVarWithLenPrefix(ByteBuf src) {
		Preconditions.checkArgument(src.isReadable());
		int len = src.readInt();
		ByteBuf ret = src.readSlice(len);
		return ret;
	}
	/**
	 * 批量记录索引位置
	 * @param buffers
	 */
	public static void markIndex(ByteBuf... buffers) {
		for(ByteBuf buffer : buffers) {
			// 记录bytebuf信息
			buffer.markReaderIndex();
			buffer.markWriterIndex();
		}
	}
	/**
	 * 批量恢复索引位置
	 * @param buffers
	 */
	public static void resetIndex(ByteBuf... buffers) {
		for(ByteBuf buffer : buffers) {
			// 恢复bytebuf信息
			buffer.resetReaderIndex();
			buffer.resetWriterIndex();
		}
	}
	/**
	 * ByteBuf内容生成字符串
	 * @param buf
	 * @return
	 */
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
	/**字符串转byteBuf
	 * @param str
	 * @return
	 */
	public static ByteBuf str2Buf(String str) {
		return Unpooled.wrappedBuffer(str.getBytes());
	}

}
