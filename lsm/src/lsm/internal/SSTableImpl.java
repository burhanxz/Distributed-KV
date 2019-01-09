package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.Block;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.base.ByteBufUtils;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;

public class SSTableImpl implements SSTable{
	private final int FOOTER_SIZE = 48;
	private final FileChannel channel;
	private Block indexBlock;
	private Block metaIndexBlock;
	/**
	 * sstable对应文件信息
	 */
	private final long fileNumber;
	private volatile boolean isClosed = false;;
	
	public SSTableImpl(File databaseDir, FileMetaData fileMetaData) throws IOException {
		this(databaseDir, fileMetaData.getNumber());
	}
	
	public SSTableImpl(File databaseDir, Long fileNumber) throws IOException {
		this.fileNumber = fileNumber;
		File file = FileUtils.newSSTableFile(databaseDir, fileNumber);
		channel = new RandomAccessFile(file, "rw").getChannel();
		// 将文件尾部48B数据读取到内存作为Footer数据
		ByteBuf footer = PooledByteBufAllocator.DEFAULT.buffer(FOOTER_SIZE);
		ByteBufUtils.write(channel.position(channel.size() - FOOTER_SIZE), footer);
		// 根据Footer获取indexBlock handle和metaIndexBlock handle
		ByteBuf indexBlockHandle = footer.slice(footer.readerIndex(), Integer.BYTES * 2);
		ByteBuf metaIndexBlockHandle = footer.slice(footer.readerIndex() + Integer.BYTES * 2, Integer.BYTES * 2);
		// 读取index block和meta index block字节
		int indexBlockOffset = indexBlockHandle.readInt();
		int indexBlockSize = indexBlockHandle.readInt();
		int metaIndexBlockOffset = metaIndexBlockHandle.readInt();
		int metaIndexBlockSize = metaIndexBlockHandle.readInt();
		ByteBuf indexBlockBuffer = PooledByteBufAllocator.DEFAULT.buffer(indexBlockSize);
		ByteBuf metaIndexBlockBuffer = PooledByteBufAllocator.DEFAULT.buffer(metaIndexBlockSize);
		ByteBufUtils.read(channel.position(indexBlockOffset), indexBlockBuffer);
		ByteBufUtils.read(channel.position(metaIndexBlockOffset), metaIndexBlockBuffer);
		// 获取Block对象
		indexBlock = new BlockImpl(indexBlockBuffer);
		metaIndexBlock = new BlockImpl(metaIndexBlockBuffer);
	}
	
	@Override
	public Block openBlock(int blockOffset, int blockSize) throws IOException {
		ByteBuf blockBuffer = PooledByteBufAllocator.DEFAULT.buffer(blockSize);
		ByteBufUtils.read(channel.position(blockOffset), blockBuffer);
		Block block = new BlockImpl(blockBuffer);
		return block;
	}
	
	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		Preconditions.checkState(isClosed);
		return null;
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
	 * 两层迭代器，在两层迭代器中，level1中的迭代器指向的是一个容器，level2中的迭代器才指向真正的元素。
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
				// 取出data block及其迭代器
				dataBlockIterator = getCurrentDataBlockIterator();
				// 在此data block中获取key
				dataBlockIterator.seek(key);
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
		
		private SeekingIterator<ByteBuf, ByteBuf> getCurrentDataBlockIterator(){
			// 获取含有offset和size信息的字节数组
	        ByteBuf handle = indexBlockIterator.next().getValue();
	        // 获取blockOffset和blockSize信息
	        int blockOffset = handle.getInt(handle.readerIndex());
	        int blockSize = handle.getInt(handle.readerIndex() + Integer.BYTES);
	        // 根据offset和size获取block
	        Block dataBlock = null;
			try {
				dataBlock = openBlock(blockSize, blockOffset);
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
	        boolean currentHasNext = false;
	        while (true) {
	        	// 如果没有data block迭代器，尝试获取
	            if (dataBlockIterator != null) {
	                currentHasNext = dataBlockIterator.hasNext();
	            }
	            // 如果获取失败，使用下一个index block迭代器，再次获取data block迭代器
	            if (!(currentHasNext)) {
	                if (indexBlockIterator.hasNext()) {
	                	dataBlockIterator = getCurrentDataBlockIterator();
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
