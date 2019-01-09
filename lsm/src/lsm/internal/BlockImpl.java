package lsm.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.Block;
import lsm.SeekingIterator;

public class BlockImpl implements Block{
	private ByteBuf block;
	private ByteBuf data;
	private List<Integer> restartPointers;
	private int restarts;
	
	public BlockImpl(ByteBuf block) {
		// 至少有一个重启点数目信息
		Preconditions.checkArgument(block.readableBytes() > Integer.BYTES);
		this.block = block.slice();
		// 获取数据尾端4B作为重启点数目
		restarts = block.slice(block.writerIndex() - Integer.BYTES, Integer.BYTES).readInt();
		restartPointers = new ArrayList<>(restarts);
		// 重启点占数据总大小
		int restartPointerSize = restarts * 4;
		// 获取重启点字节数据
		ByteBuf restartBuffer = block.slice(block.writerIndex() - Integer.BYTES - restartPointerSize, restartPointerSize);
		// 载入重启点信息
		for(int i = 0; i < restarts; i++) {
			restartPointers.set(i, restartBuffer.readInt());
		}
		// 实际有效k-v数据
		this.data = block.slice(block.readerIndex(), block.readableBytes() - Integer.BYTES - restartPointerSize);
	}
	
	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getDataSize() {
		return data.readableBytes();
	}

	@Override
	public int getFullBlockSize() {
		return block.readableBytes();
	}
	
	class BlockIterator implements SeekingIterator<ByteBuf, ByteBuf>{

		@Override
		public boolean hasNext() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> next() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> peek() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void seek(ByteBuf key) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void seekToFirst() {
			// TODO Auto-generated method stub
			
		}
		
	}

}
