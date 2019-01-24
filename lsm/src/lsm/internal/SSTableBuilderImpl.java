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
 * Footer的典型格式(48B)
 * |   index block handle   | 存放index block的handle信息
 * | meta index block handle| 存放meta index block的handle信息
 * |         padding        | 填充，使得其上两个区域大小达到40B
 * |          magic         | 魔数，8B
 * @author bird
 *
 */
public class SSTableBuilderImpl implements SSTableBuilder {
	/**
	 * 填充区原始大小
	 */
	private static final int PADDING_CONSTANTS = 40;
	/**
	 * Footer魔数
	 */
	private static final long MAGIC_NUMBER = 0xdb4775248b80fb57L;
	/**
	 * 默认的meta block filter参数, 11 即每 1 << 11 B的数据产生一个filter数据
	 */
	private static final int DEFAULT_FILTER_BASE_LG = 11;
	/**
	 * block中重启点间隔
	 */
	private final int interval;
	/**
	 * block限制大小
	 */
	private final int blockSize;
	/**
	 * table绑定文件通道
	 */
	private final FileChannel fileChannel;
	/**
	 * 存放k-v数据的block
	 */
	private BlockBuilder dataBlock;
	/**
	 * 存放过滤信息
	 */
	private MetaBlockBuilder metaBlock;
	/**
	 * 存放data block的索引信息
	 */
	private BlockBuilder indexBlock; 
	/**
	 * 存放meta block的索引信息
	 */
	private BlockBuilder metaIndexBlock;
	/**
	 * 判断当前data block是否完成数据写入
	 */
	private volatile boolean finishDataBlock = false;
	/**
	 * 记录上一个block的大小
	 */
	private volatile int lastBlockSize;
	/**
	 * 判断table builder是否写入完成
	 */
	private volatile boolean isFinished = false;
	/**
	 * 上一个写入的数据
	 */
	private ByteBuf lastKey;
	
	public SSTableBuilderImpl(int interval, int blockSize, FileChannel fileChannel) {
		Preconditions.checkState(fileChannel.isOpen());
		this.interval = interval;
		this.blockSize = blockSize;
		this.fileChannel = fileChannel;
		// 初始化data block builder 
		dataBlock = new BlockBuilderImpl(interval);
		// 初始化meta block builder
		metaBlock = new MetaBlockBuilderImpl(DEFAULT_FILTER_BASE_LG, new BloomFilter());
		// 初始化indexBlock builder
		indexBlock = new BlockBuilderImpl(interval);
		// 初始化meta index block
		metaIndexBlock = new BlockBuilderImpl(interval);
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
			handleBlock(key);
		}
		// 向data block中添加数据
		dataBlock.add(key, value);
		// 向meta block中添加数据
		metaBlock.addKey(key);
		// 判断data block大小是否达到限制，同时更新last block size
		if((lastBlockSize = dataBlock.size()) >= blockSize) {
			// 将data block持久化到filechannel
			serializeDataBlock();
			lastKey = key;
		}

	}
	
	/**
	 * 将当前data block序列化到文件
	 * @throws IOException
	 */
	private void serializeDataBlock() throws IOException {
		Preconditions.checkNotNull(dataBlock);
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
	private void handleBlock(ByteBuf currentKey) throws IOException {
		Preconditions.checkState(dataBlock == null);
		Preconditions.checkNotNull(lastKey);
		// 计算分界key
		ByteBuf divide = getSeparator(lastKey, currentKey);
		// 生成索引块
		int blockSize = lastBlockSize;
		int blockOffset = (int) fileChannel.position() - blockSize;
		// 获取handle信息
		ByteBuf handle = getHandle(blockOffset, blockSize);
		// 写入到index block
		indexBlock.add(divide, handle);
		// 重置data block状态，新建data block
		finishDataBlock = !finishDataBlock;
		dataBlock = new BlockBuilderImpl(interval);
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
	
    /**
     * TODO 待测试
     * 获取不小于start并且不大于limit的ByteBuf作为两个data block之间的分隔符
     * @param start 下界
     * @param limit 上界
     * @return
     */
    public ByteBuf getSeparator(ByteBuf start, ByteBuf limit)
    {
    	Preconditions.checkNotNull(start);
    	if(limit == null) {
    		return start.slice();
    	}
    	ByteBuf separator = null;
    	ByteBuf initialStart = start;
    	// 计算公共前缀长度
    	limit = limit.slice();
		start = start.slice();
		// 公共前缀长度
		int len = 0;
		// 当两个bytebuf还有剩余数据的时候，逐一查看其字节是否相等
		while(start.readableBytes() > 0 && limit.readableBytes() > 0) {
			if(start.readByte() == limit.readByte()) {
				len++;
			}
			else {
				break;
			}
		}
    	// 如果start完全是limit的前缀，则以start为分隔符
		if(start.readableBytes() == 0) {
			separator = initialStart.slice();
		}
    	// 把start非公共部分的第一个字节+1，得到一个新的key,如果这个key不大于limit，则key为分隔符
		else {
			// 获取当前位置的字节
			Byte oldByte = start.getByte(len);
			// 如果字节达到最大值
			if(oldByte == Byte.MAX_VALUE) {
				separator = initialStart.slice();
			}
			else {
				Byte newByte = (byte) (oldByte + 1);
				// 如果字节+1后超过了limit同位置上的数据
				if(newByte > limit.getByte(len)) {
					separator = initialStart.slice();
				}
				else {
					// TODO 拷贝可读数据，将第一个字节设置为newByte
					separator = start.copy();
					separator.setByte(0, newByte);
				}
			}
		}
    	// 否则，返回start作为分隔符
        return start;
    }
	@Override
	public void finish() throws IOException {
		// 如果dataBlock
		if(dataBlock != null) {
			serializeDataBlock();
		}
		// 对最后一个data block写入index block和meta block信息
		if(finishDataBlock) {
			handleBlock(null);
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
		
		// 此处实际上就是footer的序列化过程，其中padding, index block handle和meta index block handle共占40B
		// footer中magic占8B，footer总大小48B
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
