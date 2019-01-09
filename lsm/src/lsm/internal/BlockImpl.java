package lsm.internal;

import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.Block;
import lsm.SeekingIterator;

public class BlockImpl implements Block{

	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getOffset() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getDataSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getFullBlockSize() {
		// TODO Auto-generated method stub
		return 0;
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
