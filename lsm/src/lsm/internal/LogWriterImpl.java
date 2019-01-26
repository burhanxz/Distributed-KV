package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.LogWriter;
import lsm.base.FileUtils;
import lsm.base.InternalKey;

/**
 * 利用mmap实现的log writer
 * @author bird
 *
 */
public class LogWriterImpl implements LogWriter {
	/**
	 * log文件 以32KB为单位存取
	 */
	private final static int LOG_BLOCK_SIZE = 1 << 15;
	/**
	 * 每个线程持有一个32KB的缓冲区用来将bytebuf中的数据写入mmap
	 */
	private final static ThreadLocal<byte[]> caches = new ThreadLocal<byte[]>(){
		@Override
		protected byte[] initialValue() {
			return new byte[LOG_BLOCK_SIZE];
		}};
	/**
	 * log writer绑定文件
	 */
	private final FileChannel channel;
	/**
	 * 文件编号
	 */
	private final long fileNumber;
	/**
	 * mmap结构用于数据持久化
	 */
	private MappedByteBuffer mmap;
	/**
	 * 记录上一处mmap分配位置
	 */
	private long lastPos;
	@SuppressWarnings("resource")
	public LogWriterImpl(File databaseDir, long fileNumber, boolean isManifest) throws IOException {
		Preconditions.checkArgument(fileNumber > 2);
		this.fileNumber = fileNumber;
		// 新建manifest或普通log文件
		File file = FileUtils.newLogFile(databaseDir, fileNumber, isManifest);
		// 绑定file channel
		channel = new RandomAccessFile(file, "rw").getChannel();
		// 获取mmap
		mmap = channel.map(MapMode.READ_WRITE, 0, LOG_BLOCK_SIZE);
		// 初始化位置信息
		lastPos = 0;
	}

	/**
	 * 确保mmap空间满足需求
	 * @param need 需要的空间
	 * @throws IOException
	 */
	private void applyFor(int need) throws IOException {
		// 如果mmap剩余空间已经不满足需求
		if(mmap.remaining() < need) {
			// 将page cache中的数据刷进硬盘
			mmap.force();
			// 更新位置并且重新申请mmap
			lastPos += LOG_BLOCK_SIZE;
			mmap = channel.map(MapMode.READ_WRITE, lastPos, LOG_BLOCK_SIZE);
		}
	}
	@Override
	public void addRecord(ByteBuf record, boolean force) throws IOException{
		// 计算record占用空间
		int need = record.readableBytes();
		// 数据写入缓冲区
		byte[] cache = caches.get();
		record.readBytes(cache, 0, need);
		synchronized(this) {
			// 申请写入空间
			applyFor(need);
			// 将数据写入mmap
			mmap.put(cache);
		}
	}

	@Override
	public void addRecord(InternalKey internalKey, ByteBuf value, boolean force) throws IOException{
		// 申请bytebuf，至少需要3个大小信息(4B) 和一个seq & type信息 (8B)
		ByteBuf record = PooledByteBufAllocator.DEFAULT.buffer(Integer.BYTES * 3 + Long.BYTES, LOG_BLOCK_SIZE);
		// 具体格式详见LogWriter接口注释
		record.writeInt(internalKey.size() + value.readableBytes());
		record.writeInt(internalKey.size());
		record.writeBytes(internalKey.encode().slice());
		record.writeInt(value.readableBytes());
		record.writeBytes(value.slice());
		addRecord(record, force);
	}
	
	@Override
	public synchronized void close() throws IOException {
		if(channel.isOpen()) {
			// 获取channel当前位置
			long pos = channel.position();
			// 确保channel的大小是LOG_BLOCK_SIZE的整数倍
			long mod = 0;
			if((mod = pos % LOG_BLOCK_SIZE) != 0) {
				pos += mod;
			}
			// 截断filechannel,确保其大小为LOG_BLOCK_SIZE整数倍
			channel.truncate(pos);
			Preconditions.checkArgument(channel.size() % LOG_BLOCK_SIZE == 0);
			// 关闭channel
			channel.close();
		}
	}
	@Override
	public long getFileNumber() {
		return fileNumber;
	}
}
