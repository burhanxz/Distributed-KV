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
}
