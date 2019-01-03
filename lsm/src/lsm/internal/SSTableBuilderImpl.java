package lsm.internal;

import java.nio.channels.FileChannel;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.SSTableBlockBuilder;
import lsm.SSTableBuilder;
import lsm.base.InternalKey;

/**
 * sstable典型格式:
 * |   data block  | 多个存放数据的block,存放用户(k,v)
 * |  index block  | 一个专门存放index的block，存放 (分界k, 索引块编码)
 * |  bloom filter | 多个bloom filter字节数组，数量和data block数量一致
 * |metaIndex block| 一个专门存放bloom filter index的block，存放 (filter名， filter位置)
 * |     Footer    | 指向各个分区的位置和大小
 * 
 * 
 * @author bird
 *
 */
public class SSTableBuilderImpl implements SSTableBuilder {
	private final int interval;
	private final int blockSize;
	private final FileChannel fileChannel;
	private SSTableBlockBuilder dataBlock;
	private SSTableBlockBuilder indexBlock; 
	private SSTableBlockBuilder metaIndexBlock;
	private Footer footer;

	public SSTableBuilderImpl(int interval, int blockSize, FileChannel fileChannel) {
		super();
		this.interval = interval;
		this.blockSize = blockSize;
		this.fileChannel = fileChannel;
	}
	
	@Override
	public void add(Entry<InternalKey, ByteBuf> entry) {
		add(entry.getKey().encode(), entry.getValue());
	}

	@Override
	public void add(ByteBuf key, ByteBuf value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void abandon() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getFileSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	
}
