package lsm.internal;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.BlockBuilder;
import lsm.SSTableBuilder;
import lsm.base.InternalKey;

/**
 * sstable典型格式:
 * |   data block  | 多个存放数据的block,存放用户(k,v)
 * |  index block  | 一个专门存放index的block，存放 (分界k, 索引块编码)
 * |   meta block  | 多个bloom filter字节数组，每2KB大小的data block对应一个bloom filter
 * |metaIndex block| 一个专门存放bloom filter index的block，存放 (filter名， filter位置)
 * |     Footer    | 指向各个分区的位置和大小
 * 
 * @author bird
 *
 */
public class SSTableBuilderImpl implements SSTableBuilder {
	private final int interval;
	private final int blockSize;
	private final FileChannel fileChannel;
	private BlockBuilder dataBlock;
	private BlockBuilder indexBlock; 
	private BlockBuilder metaIndexBlock;
	private Footer footer;
	
	private volatile boolean shouldWriteIndexBlock = false;
	private volatile int lastBlockSize;
	
	public SSTableBuilderImpl(int interval, int blockSize, FileChannel fileChannel) {
		this.interval = interval;
		this.blockSize = blockSize;
		this.fileChannel = fileChannel;
		// TODO
	}
	
	@Override
	public void add(Entry<InternalKey, ByteBuf> entry) throws IOException {
		add(entry.getKey().encode(), entry.getValue());
	}

	@Override
	public void add(ByteBuf key, ByteBuf value) throws IOException {
		// 在写完一个data block之后，新写一个data block之前，写index block
		if(shouldWriteIndexBlock) {
			// TODO 计算分界key
			ByteBuf divide = null;
			// 生成索引块
			long offset = fileChannel.position();
			int size = lastBlockSize;
			// TODO 申请内存
			ByteBuf index = PooledByteBufAllocator.DEFAULT.buffer(Long.BYTES + Integer.BYTES);
			index.writeLong(offset);
			index.writeInt(size);
			// 写入到index block
			indexBlock.add(divide, index);
			// TODO 重置
			shouldWriteIndexBlock = !shouldWriteIndexBlock;
			dataBlock = null;
		}
		// 向data block中添加数据
		dataBlock.add(key, value);
		// 判断data block大小是否达到限制，同时更新last block size
		if((lastBlockSize = dataBlock.size()) >= blockSize) {
			// TODO 将data block持久化到filechannel
			// 指示下一步应当写index block
			shouldWriteIndexBlock = true;
		}

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
