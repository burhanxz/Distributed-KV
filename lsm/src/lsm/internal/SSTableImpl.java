package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.Block;
import lsm.MetaBlock;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.base.ByteBufUtils;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;

public class SSTableImpl implements SSTable{
	private static final Logger LOG = LoggerFactory.getLogger(MetaBlockImpl.class);
	/**
	 * footer所占大小
	 */
	private final int FOOTER_SIZE = 48;
	/**
	 * sstable绑定的文件
	 */
	private final FileChannel channel;
	/**
	 * index block 存放index handle
	 */
	private Block indexBlock;
	/**
	 * meta index block 存放meta index handle
	 */
	private Block metaIndexBlock;
	private MetaBlock metaBlock;
	private int metaBlockOffset;
	/**
	 * index block位置
	 */
	private int indexBlockOffset;
	/**
	 * metaIndex block位置
	 */
	private int metaIndexBlockOffset;
	/**
	 * sstable文件编号
	 */
	private final long fileNumber;
	/**
	 * sstable文件大小
	 */
	private final long fileSize;
	/**
	 * 对象状态
	 */
	private volatile boolean isClosed = false;
	
	public SSTableImpl(File databaseDir, Long fileNumber) throws IOException {
		this.fileNumber = fileNumber;
		Preconditions.checkNotNull(fileNumber > 1);
		File file = FileUtils.newSSTableFile(databaseDir, fileNumber);
		channel = new RandomAccessFile(file, "r").getChannel();
		this.fileSize = channel.size();
		// 初始化信息
		init();
	}
	
	/**
	 * 初始化table信息
	 * 
	 * @throws IOException
	 */
	private void init() throws IOException {
		// 将文件尾部48B数据读取到内存作为Footer数据
		ByteBuf footer = PooledByteBufAllocator.DEFAULT.buffer(FOOTER_SIZE);
		ByteBufUtils.read(channel.position(channel.size() - FOOTER_SIZE), footer);
		// 根据Footer获取indexBlock handle和metaIndexBlock handle
		ByteBuf indexBlockHandle = footer.slice(footer.readerIndex(), Integer.BYTES * 2);
		ByteBuf metaIndexBlockHandle = footer.slice(footer.readerIndex() + Integer.BYTES * 2, Integer.BYTES * 2);
		// 读取index block和meta index block字节
		indexBlockOffset = indexBlockHandle.readInt();
		int indexBlockSize = indexBlockHandle.readInt();
		metaIndexBlockOffset = metaIndexBlockHandle.readInt();
		int metaIndexBlockSize = metaIndexBlockHandle.readInt();
		// 为index block和meta index block分配内存
		ByteBuf indexBlockBuffer = PooledByteBufAllocator.DEFAULT.buffer(indexBlockSize);
		ByteBuf metaIndexBlockBuffer = PooledByteBufAllocator.DEFAULT.buffer(metaIndexBlockSize);
		// 从文件读入数据
		ByteBufUtils.read(channel.position(indexBlockOffset), indexBlockBuffer);
		ByteBufUtils.read(channel.position(metaIndexBlockOffset), metaIndexBlockBuffer);
		// 获取Block对象
		indexBlock = new BlockImpl(indexBlockBuffer);
		metaIndexBlock = new BlockImpl(metaIndexBlockBuffer);
		SeekingIterator<ByteBuf, ByteBuf> metaIndexBlockIterator = metaIndexBlock.iterator();
		// 获取含有offset和size信息的字节数组
		Preconditions.checkState(metaIndexBlockIterator.hasNext());
		
        ByteBuf handle = metaIndexBlockIterator.next().getValue().slice();
        // 获取blockOffset和blockSize信息
        metaBlockOffset = handle.readInt();
        int metaBlockSize = handle.readInt();
        // 获取filter数据
        ByteBuf filterResult = PooledByteBufAllocator.DEFAULT.buffer(metaBlockSize);
        ByteBufUtils.read(channel.position(metaBlockOffset), filterResult);
        // 新建metaBlock
        metaBlock = new MetaBlockImpl(new BloomFilter(), filterResult);
        LOG.debug("fileNumber = " + fileNumber);
        LOG.debug("fileSize = " + fileSize);
        LOG.debug("metaBlockOffset = " + metaBlockOffset);
        LOG.debug("indexBlockOffset = " + indexBlockOffset);
        LOG.debug("metaIndexBlockOffset = " + metaIndexBlockOffset);
	}
	
	@Override
	public Block openBlock(int blockOffset, int blockSize) throws IOException {
		Preconditions.checkState(!isClosed);
		Preconditions.checkArgument(blockOffset >= 0 && blockOffset < metaBlockOffset, String.format("block offset = %d 越界", blockOffset));
		Preconditions.checkArgument(blockSize > 0);
		// 为data block分配内存
		ByteBuf blockBuffer = PooledByteBufAllocator.DEFAULT.buffer(blockSize);
		// 读入数据
		ByteBufUtils.read(channel.position(blockOffset), blockBuffer);
		Block block = new BlockImpl(blockBuffer);
		return block;
	}
	
	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		Preconditions.checkState(!isClosed);
		return new TwoLevelIterator(indexBlock.iterator());
	}

	@Override
	public void close() throws IOException {
		// 当channel存在且打开时
		if(channel != null && channel.isOpen()) {
			// 关闭channel并且设置已关闭标识
			channel.close();
			isClosed = true;
		}
	}
	
	/**
	 * 两层迭代器，在两层迭代器中，level1中的迭代器指向的是level2迭代器，level2中的迭代器才指向真正的元素。
	 * @author bird
	 *
	 */
	class TwoLevelIterator implements SeekingIterator<ByteBuf, ByteBuf>{
		/**
		 * sstable中实际存储的k-v数据，当next为空时，认为整个迭代器处于初始位置
		 */
		private Entry<ByteBuf, ByteBuf> next;
		/**
		 * level1迭代器，指向index block中的数据，包含数据位置信息
		 */
		private SeekingIterator<ByteBuf, ByteBuf> indexBlockIterator;
		/**
		 * level2迭代器，指向所有data block中的数据，包含真实数据
		 */
		private SeekingIterator<ByteBuf, ByteBuf> dataBlockIterator;
		/**
		 * 判断seek是否成功
		 */
		private boolean seek = true;
		
		TwoLevelIterator(SeekingIterator<ByteBuf, ByteBuf> indexBlockIterator){
			this.indexBlockIterator = indexBlockIterator;
		}
		@Override
		public boolean hasNext() {
			// 如果next为空，尝试获取下一个元素
	        if (next == null) {
	        	next = getNextElement();
	        }
	        // 检验下一个元素是否为空
	        return next != null;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> next() {
			// 如果next元素为空，尝试获取下一个元素
	        if (next == null) {
	        	next = getNextElement();
	        	// 如果元素依然为空，抛出异常
	            if (next == null) {
	            	 throw new NoSuchElementException();
	            }
	        }
	        // next置空，返回结果
	        Entry<ByteBuf, ByteBuf> result = next;
	        next = null;
	        return result;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> peek() {
			// 相比于next()，不置空元素
	        if (next == null) {
	        	next = getNextElement();
	            if (next == null) {
	                throw new NoSuchElementException();
	            }
	        }
	        return next;
		}

		@Override
		public void seek(ByteBuf key) {
	        next = null;
			// 先利用level1容器迭代器寻找位置
			indexBlockIterator.seek(key);
			if(indexBlockIterator.hasNext()) {
    			// 获取含有offset和size信息的字节数组
    	        ByteBuf handle = indexBlockIterator.next().getValue().slice();
    	        // 获取blockOffset和blockSize信息
    	        int blockOffset = handle.readInt();
    	        int blockSize = handle.readInt();
            	if(!metaBlock.keyMayMatch(blockOffset, key)) {
            		// 如果bloomfilter确定没有此key，则置空
            		dataBlockIterator = null;
            		// 没有找到
            		seek = false;
            	}
            	else {
    				// 取出data block及其迭代器
    				dataBlockIterator = getCurrentDataBlockIterator(blockOffset, blockSize);
    				// 在此data block中获取key
    				dataBlockIterator.seek(key);
            	}
			}
			else {
				// 如果level1没有寻找到可能含有key的index block条目，则将data block置空
				dataBlockIterator = null;
			}
		}

		@Override
		public void seekToFirst() {
			next = null;
			// level1 容器迭代器移动到初始位置
			indexBlockIterator.seekToFirst();
			// level2 数据迭代器清空
			dataBlockIterator = null;
		}

		/**
		 * 获取当前index block迭代器位置下， 对应的data block迭代器
		 * @param blockOffset
		 * @param blockSize
		 * @return
		 */
		private SeekingIterator<ByteBuf, ByteBuf> getCurrentDataBlockIterator(int blockOffset, int blockSize){
	        // 根据offset和size获取block
	        Block dataBlock = null;
			try {
				dataBlock = openBlock(blockOffset, blockSize);
			} catch (IOException e) {
				e.printStackTrace();
			}
	        // 根据block获取迭代器
	        return dataBlock.iterator();
		}
		
		/**
		 * 核心实现，通过level1迭代器获取level2迭代器，进而获取实际k-v数据
		 * @return
		 */
		private Entry<ByteBuf, ByteBuf> getNextElement() {
			// 上一次seek操作失败
			if(dataBlockIterator == null && !seek) {
				return null;
			}
	        boolean currentHasNext = false;
	        while (true) {
	        	// 如果没有data block迭代器，尝试获取
	            if (dataBlockIterator != null) {
	                currentHasNext = dataBlockIterator.hasNext();
	            }
	            // 如果获取失败，使用下一个index block迭代器，再次获取data block迭代器
	            if (!(currentHasNext)) {
	                if (indexBlockIterator.hasNext()) {
	        			// 获取含有offset和size信息的字节数组
	        	        ByteBuf handle = indexBlockIterator.next().getValue().slice();
	        	        // 获取blockOffset和blockSize信息
	        	        int blockOffset = handle.readInt();
	        	        int blockSize = handle.readInt();
	        	        dataBlockIterator = getCurrentDataBlockIterator(blockOffset, blockSize);
	                }
	                else {
	                    break;
	                }
	            }
	            else {
	                break;
	            }
	        }
	        // 正常情况下，直接通过data block iterator获取数据
	        if (currentHasNext) {
	            return dataBlockIterator.next();
	        }
	        else {
	            // 如果最终获取失败，将data block清空
	        	dataBlockIterator = null;
	            return null;
	        }
		}
		
	}

}
