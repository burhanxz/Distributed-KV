package lsm.base;

import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * fileMetaData序列化格式
 * | number(8B) | fileSize(8B) | smallest size(4B) | smallest | largest size(4B) | largest |
 * @author bird
 *
 */
public class FileMetaData {
    /**
     * .sst文件编号
     */
    private final long number;

    /**
     * File size in bytes
     */
    private final long fileSize;

    /**
     * Smallest internal key served by table
     */
    private final InternalKey smallest;

    /**
     * Largest internal key served by table
     */
    private final InternalKey largest;

    /**
     * Seeks allowed until compaction
     * 允许的最大查找次数
     */
    private final AtomicInteger allowedSeeks = new AtomicInteger(1 << 30);

	public FileMetaData(long number, long fileSize, InternalKey smallest, InternalKey largest) {
		this.number = number;
		this.fileSize = fileSize;
		this.smallest = smallest;
		this.largest = largest;
	}
	
	/**
	 * 严格按照file meta data格式序列化
	 * @return
	 */
	public ByteBuf encode() {
		ByteBuf smallestKeyBuffer = smallest.encode();
		ByteBuf largestKeyBuffer = largest.encode();
		ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(Long.BYTES * 2 + Integer.BYTES * 2 + smallestKeyBuffer.readableBytes() + largestKeyBuffer.readableBytes());
		buffer.writeLong(number);
		buffer.writeLong(fileSize);
		buffer.writeInt(smallestKeyBuffer.readableBytes());
		buffer.writeBytes(smallestKeyBuffer);
		buffer.writeInt(largestKeyBuffer.readableBytes());
		buffer.writeBytes(largestKeyBuffer);
		return null;
	}

	public long getNumber() {
		return number;
	}

	public long getFileSize() {
		return fileSize;
	}

	public InternalKey getSmallest() {
		return smallest;
	}

	public InternalKey getLargest() {
		return largest;
	}

}
