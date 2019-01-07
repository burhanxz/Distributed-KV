package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.NoSuchElementException;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;

public class SSTableImpl implements SSTable{
	private final FileChannel channel;
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
	
	private class TableIterator implements SeekingIterator<ByteBuf, ByteBuf>{
		private Entry<ByteBuf, ByteBuf> next;
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
	        seekInternal(key);
		}

		@Override
		public void seekToFirst() {
			next = null;
	        seekToFirstInternal();
		}

	    private void seekToFirstInternal() {
	    	
	    }
	    
	    private void seekInternal(ByteBuf targetKey) {
	    	
	    }

	    private Entry<ByteBuf, ByteBuf> getNextElement(){
	    	return null;
	    }
		
	}
}
