package lsm.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.BlockBuilder;
import lsm.MetaBlockBuilder;
import lsm.SSTableBuilder;
import lsm.base.ByteBufUtils;
import lsm.base.InternalKey;

/**
 * sstable典型格式:
 * |   data block  | 多个存放数据的block,存放用户(k,v)
 * |   meta block  | 多个bloom filter字节数组，每2KB大小的data block对应一个bloom filter
 * |  index block  | 一个专门存放index的block，存放 (分界k, 索引块编码)
 * |metaIndex block| 一个专门存放bloom filter index的block，只有一条记录,存放 (filter名， filter位置)
 * |     Footer    | 指向各个分区的位置和大小
 * 
 * @author bird
 *
 */
public class SSTableBuilderImpl implements SSTableBuilder {
	private static final int PADDING_CONSTANTS = 40;
	private static final long MAGIC_NUMBER = 0xdb4775248b80fb57L;
	private final int interval;
	private final int blockSize;
	private final FileChannel fileChannel;
	private BlockBuilder dataBlock;
	private MetaBlockBuilder metaBlock;
	private BlockBuilder indexBlock; 
	private BlockBuilder metaIndexBlock;
	
	private volatile boolean finishDataBlock = false;
	private volatile int lastBlockSize;

	private volatile boolean isFinished = false;
	
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
		Preconditions.checkState(!isFinished, "builder过程已经结束");
		// 在写完一个data block之后，新写一个data block之前，写index block
		if(finishDataBlock) {
			handleBlock();
		}
		// 向data block中添加数据
		dataBlock.add(key, value);
		// 向meta block中添加数据
		metaBlock.addKey(key);
		// 判断data block大小是否达到限制，同时更新last block size
		if((lastBlockSize = dataBlock.size()) >= blockSize) {
			// 将data block持久化到filechannel
			serializeDataBlock();
		}

	}
	
	/**
	 * 将当前data block序列化到文件
	 * @throws IOException
	 */
	private void serializeDataBlock() throws IOException {
		// 获取data block字节数据
		ByteBuf dataBlockBytes = dataBlock.finish();
		// data block写入文件
		ByteBufUtils.write(fileChannel, dataBlockBytes);
		// 清除data block
		dataBlock = null;
		// 指示下一步应当写index block
		finishDataBlock = true;
	}
	
	/**
	 * 当一个block写完的时候，写入meta block和index block
	 * @throws IOException
	 */
	private void handleBlock() throws IOException {
		// TODO 计算分界key
		ByteBuf divide = null;
		// 生成索引块
		int blockSize = lastBlockSize;
		int blockOffset = (int) fileChannel.position() - blockSize;
		// 获取handle信息
		ByteBuf handle = getHandle(blockOffset, blockSize);
		// 写入到index block
		indexBlock.add(divide, handle);
		// TODO 重置
		finishDataBlock = !finishDataBlock;
		dataBlock = null;
		// 记录前一个data block的filter信息
		metaBlock.startBlock((int)fileChannel.position());
	}
	
	/**
	 * handle指的是位置和大小信息
	 * @param offset 偏置,即起始位置
	 * @param size 大小
	 * @return handle的字节数据
	 */
	private ByteBuf getHandle(int offset, int size) {
		Preconditions.checkArgument(offset >= 0 && size >= 0);
		ByteBuf handle = PooledByteBufAllocator.DEFAULT.buffer(2 * Long.BYTES);
		handle.writeInt(offset);
		handle.writeInt(size);
		return handle;
	}
	
	@Override
	public void finish() throws IOException {
		if(dataBlock != null) {
			serializeDataBlock();
		}
		// 对最后一个data block写入index block和meta block信息
		if(finishDataBlock) {
			handleBlock();
		}
		// 获取meta block及其handle信息
		ByteBuf metaBlockBytes = metaBlock.finish();
		ByteBuf metaBlockHandle = getHandle((int) fileChannel.position(), metaBlockBytes.readableBytes());
		// 写入meta index block信息
		// TODO key为filter policy的名字
		metaIndexBlock.add(null, metaBlockHandle);
		//将meta block序列化到文件
		ByteBufUtils.write(fileChannel, metaBlockBytes);
		// 获取index block及其handle
		ByteBuf indexBlockBytes = indexBlock.finish();
		ByteBuf indexBlockHandle = getHandle((int) fileChannel.position(), indexBlockBytes.readableBytes());
		// 将index block序列化到文件
		ByteBufUtils.write(fileChannel, indexBlockBytes);
		// 获取meta index block及其handle
		ByteBuf metaIndexBlockBytes = metaIndexBlock.finish();
		ByteBuf metaIndexBlockHandle = getHandle((int) fileChannel.position(), metaIndexBlockBytes.readableBytes());
		// 将meta index block序列化到文件
		ByteBufUtils.write(fileChannel, metaIndexBlockBytes);

		// 获取填充数据
		ByteBuf padding = PooledByteBufAllocator.DEFAULT.buffer(PADDING_CONSTANTS - indexBlockHandle.readableBytes() - metaIndexBlockHandle.readableBytes());
		// 获取魔数
		ByteBuf magic = PooledByteBufAllocator.DEFAULT.buffer(2 * Integer.BYTES);
		magic.writeInt((int) MAGIC_NUMBER);
		magic.writeInt((int) MAGIC_NUMBER >>> 32);
		// 将indexBlockHandle, metaIndexBlockHandle, padding, magic序列化到文件
		ByteBufUtils.write(fileChannel, indexBlockHandle);
		ByteBufUtils.write(fileChannel, metaIndexBlockHandle);
		ByteBufUtils.write(fileChannel, padding);
		ByteBufUtils.write(fileChannel, magic);
		// 修改结束标志
		isFinished = true;
	}

	@Override
	public void abandon() {
		isFinished = true;
	}

	@Override
	public long getFileSize() throws IOException {
		return fileChannel.size();
	}

	
}
