package lsm.base;

import java.util.concurrent.atomic.AtomicInteger;

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

}
