package lsm.base;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.LogWriter;

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
     * 文件大小，单位：B
     */
    private final long fileSize;

    /**
     * 文件最小键
     */
    private final InternalKey smallest;

    /**
     * 文件最大键
     */
    private final InternalKey largest;
    
	public FileMetaData(long number, long fileSize, InternalKey smallest, InternalKey largest) {
		this.number = number;
		this.fileSize = fileSize;
		this.smallest = smallest;
		this.largest = largest;
	}
	// 反序列化得到file meta data
	public FileMetaData(ByteBuf bytes) {
		number = bytes.readLong();
		fileSize = bytes.readLong();
		ByteBuf smallestKeyBuf = ByteBufUtils.getVarWithLenPrefix(bytes);
		ByteBuf largestKeyBuf = ByteBufUtils.getVarWithLenPrefix(bytes);
		smallest = InternalKey.decode(smallestKeyBuf);
		largest = InternalKey.decode(largestKeyBuf);
	}
	
	/**
	 * 严格按照file meta data格式序列化
	 * @return
	 */
	public ByteBuf encode() {
		ByteBuf smallestKeyBuf = smallest.encode();
		ByteBuf largestKeyBuf = largest.encode();
		ByteBuf dst = PooledByteBufAllocator.DEFAULT.buffer(LogWriter.LOG_BLOCK_SIZE);
		ByteBufUtils.putVarLong(dst, number);
		ByteBufUtils.putVarLong(dst, fileSize);
		ByteBufUtils.putVarWithLenPrefix(dst, smallestKeyBuf);
		ByteBufUtils.putVarWithLenPrefix(dst, largestKeyBuf);
		return dst;
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

	@Override
	public String toString() {
		// 方便测试用
		return new StringBuilder("[fileMetaData***: ").append("number=").append(number).append(", size=").append(fileSize)
				.append(", small=").append(smallest.toString()).append(", large=").append(largest.toString()).append(" ***fileMetaData]").toString();
	}

}
