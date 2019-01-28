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
import lsm.base.ByteBufUtils;
import lsm.base.FileUtils;
import lsm.base.InternalKey;
import lsm.base.MmapReleaseUtil;

/**
 * 利用mmap实现的log writer
 * @author bird
 *
 */
public class LogWriterImpl implements LogWriter {
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
		Preconditions.checkArgument(fileNumber >= 1);
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
			// 释放mmap
			MmapReleaseUtil.clean(mmap);
			// 更新位置并且重新申请mmap
			lastPos += LOG_BLOCK_SIZE;
			mmap = channel.map(MapMode.READ_WRITE, lastPos, LOG_BLOCK_SIZE);
		}
	}
	@Override
	public void addRecord(ByteBuf record, boolean force) throws IOException{
		// 计算record占用空间
		int recordSize = record.readableBytes();
		byte[] cache = caches.get();
		// record大小信息写入缓冲区
		cache[0] = (byte) ((recordSize >> 24) & 0xff);
		cache[1] = (byte) ((recordSize >> 16) & 0xff);
		cache[2] = (byte) ((recordSize >> 8) & 0xff);
		cache[3] = (byte) ((recordSize) & 0xff);
		// record数据写入缓冲区
		record.readBytes(cache, Integer.BYTES, recordSize);
		// 释放record
		record.release();
		// 计算所需空间
		int need = recordSize + Integer.BYTES;
		synchronized(this) {
			// 申请写入空间
			applyFor(need);
			// 将数据写入mmap
			mmap.put(cache, 0, need);
			// 如果需要则同步刷盘
			if(force) {
				mmap.force();
			}
		}
	}

	@Override
	public void addRecord(InternalKey internalKey, ByteBuf value, boolean force) throws IOException{
		// record大小信息
		int size = internalKey.size() + value.readableBytes();
		// 申请bytebuf
		ByteBuf record = PooledByteBufAllocator.DEFAULT.buffer(Integer.BYTES * 2 + size);
		// 组织record信息，具体格式详见LogWriter接口注释
		record.writeInt(internalKey.size());
		record.writeBytes(internalKey.encode().slice());
		record.writeInt(value.readableBytes());
		record.writeBytes(value.slice());
		// 写入record信息
		addRecord(record, force);
	}

	@Override
	public synchronized void close() throws IOException {
		if(channel.isOpen()) {
			if(mmap != null) {
				// 释放mmap
				MmapReleaseUtil.clean(mmap);
			}
			// 获取channel当前位置
			long size = channel.size();
			// 确保channel的大小是LOG_BLOCK_SIZE的整数倍
			long mod = 0;
			if((mod = size % LOG_BLOCK_SIZE) != 0) {
				size += mod;
				System.out.println("size = " + size);
			}
			// 截断filechannel,确保其大小为LOG_BLOCK_SIZE整数倍
			channel.truncate(size);
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
