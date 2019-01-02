package lsm.internal;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.base.FileMetaData;

public class SSTableImpl implements SSTable{
	/**
	 * sstable对应文件信息
	 */
	private final long fileNumber;
	
	public SSTableImpl(FileMetaData fileMetaData) {
		this.fileNumber = fileMetaData.getNumber();
	}
		
	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
}
