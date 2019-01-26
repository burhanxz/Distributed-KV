package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.LogReader;
import lsm.LogWriter;
import lsm.base.FileUtils;

/**
 * Log reader 单线程实现
 * @author bird
 *
 */
public class LogReaderImpl implements LogReader{
	private final FileChannel channel;
	private final ByteBuffer buff;
	private final byte[] cache = new byte[LogWriter.LOG_BLOCK_SIZE];
	public LogReaderImpl(File databaseDir, long fileNumber, boolean isManifest) throws IOException {
		Preconditions.checkArgument(fileNumber > 2);
		// 新建manifest或普通log文件
		File file = FileUtils.newLogFile(databaseDir, fileNumber, isManifest);
		// 绑定file channel
		channel = new RandomAccessFile(file, "r").getChannel();
		buff = ByteBuffer.allocate(LogWriter.LOG_BLOCK_SIZE);
	}
	@Override
	public ByteBuf readNextRecord() throws IOException {
		// 本条record的大小
		int size = 0;
		// 如果没有数据可读或数据量不足，或读取的大小值为0，则本Block读完，一次性读取下一组32KB数据到buff中
		if(buff.remaining() < Integer.BYTES || (size = buff.getInt()) == 0) {
			// 清空buff并读入数据
			buff.clear();
			channel.read(buff);
			// 准备读出数据
			buff.flip();
			// 如果buff中可读数据不足，则返回null
			if(buff.remaining() < LogWriter.LOG_BLOCK_SIZE) {
				return null;
			}
			else {
				// 重新获取大小信息
				size = buff.getInt();
			}
		}
		// 分配record
		ByteBuf record = PooledByteBufAllocator.DEFAULT.buffer(size);
		buff.get(cache, 0, size);
		// buff数据写入record中
		record.writeBytes(cache, 0, size);
		return record;
	}
	@Override
	public void close() throws IOException {
		// 关闭file channel
		if(channel.isOpen()) {
			channel.close();
		}
		
	}

}
