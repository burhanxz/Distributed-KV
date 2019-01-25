package lsm.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map.Entry;
import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.Block;
import lsm.SeekingIterator;

public class BlockImpl implements Block{
	private static final Logger LOG = LoggerFactory.getLogger(BlockImpl.class);
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
		LOG.debug("restarts = " + restarts);
		restartPointers = new ArrayList<>(restarts);
		// 重启点占数据总大小
		int restartPointerSize = restarts * 4;
		// 获取重启点字节数据
		ByteBuf restartBuffer = block.slice(block.writerIndex() - Integer.BYTES - restartPointerSize, restartPointerSize);
		// 载入重启点信息
		for(int i = 0; i < restarts; i++) {
			restartPointers.add(restartBuffer.readInt());
		}
		// 实际有效k-v数据
		this.data = block.slice(block.readerIndex(), block.readableBytes() - Integer.BYTES - restartPointerSize);
	}
	
	@Override
	public SeekingIterator<ByteBuf, ByteBuf> iterator() {
		return new BlockIterator();
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
		/**
		 * block内部实际数据
		 */
		private ByteBuf iteratorData;
		private Entry<ByteBuf, ByteBuf> next;
		BlockIterator(){
			// 获取block实际数据的副本
			this.iteratorData = data.slice();
			// 移动到初始位置
			seekToFirst();
		}
		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> next() {
	        if (!hasNext()) {
	            throw new NoSuchElementException();
	        }
	        // 返回当前next元素
	        Entry<ByteBuf, ByteBuf> nextElement = next;

	        if (!iteratorData.isReadable()) {
	        	next = null;
	        }
	        // 更新next元素
	        else {
	        	next = readNext(next.getKey());
	        }

	        return nextElement;
		}

		@Override
		public Entry<ByteBuf, ByteBuf> peek() {
	        // 直接获取next元素
	        if (!hasNext()) {
	            throw new NoSuchElementException();
	        }
	        return next;
		}

		@Override
		public void seek(ByteBuf key) {
	        if (restarts == 0) {
	            return;
	        }
	        int left = 0;
	        int right = restarts - 1;
	        // 通过二分查找，获取key所在重启点的位置
	        while (left < right) {
	            int mid = (left + right + 1) / 2;
	            seekToRestartPosition(mid);
	            if (next.getKey().compareTo(key) < 0) {
	                left = mid;
	            }
	            else {
	                right = mid - 1;
	            }
	        }
	        // 移动到重启点位置，在此基础上逐个查找key，寻找第一个 >= key的元素
	        for (seekToRestartPosition(left); next != null; next()) {
	            if (peek().getKey().compareTo(key) >= 0) {
	                break;
	            }
	        }
		}

		@Override
		public void seekToFirst() {
			// 移动到第一个重启点位置
			seekToRestartPosition(0);
		}
	
		/**
		 * 根据上一个元素的key和当前data位置来获取下一个元素
		 * @param prevKey 上一个元素的key
		 * @return 下一个元素
		 */
		private Entry<ByteBuf, ByteBuf> readNext(ByteBuf prevKey){
			// 获取共享长度，非共享长度和value长度
			int sharedLen = iteratorData.readInt();
			int nonSharedLen = iteratorData.readInt();
			int valueLen = iteratorData.readInt();
			LOG.debug("sharedLen = " + sharedLen);
			LOG.debug("nonSharedLen = " + nonSharedLen);
			LOG.debug("valueLen = " + valueLen);
			ByteBuf keyBuffer = PooledByteBufAllocator.DEFAULT.buffer(sharedLen + nonSharedLen);
			// 将data中的数据读入key
			// TODO 待验证
			if(sharedLen > 0) {
				Preconditions.checkNotNull(prevKey);
				// 如果有共享长度，则将上一个数据的key的共享部分读取
				prevKey.slice().readBytes(keyBuffer, sharedLen);
			}
			// 读取key非共享部分
			iteratorData.readBytes(keyBuffer, nonSharedLen);
			// 获取value
			ByteBuf valueBuffer = PooledByteBufAllocator.DEFAULT.buffer(valueLen);
			iteratorData.readBytes(valueBuffer, valueLen);
			return ImmutablePair.of(keyBuffer, valueBuffer);
		}
		
		 /**
		  * 将block数据位置移动到指定重启点位置
		 * @param restartPos 重启点编号
		 */
		private void seekToRestartPosition(int restartPos) {
			 // 获取重启点位置
			 int offset = restartPointers.get(restartPos);
			 // 将指针移动到重启点位置
			 iteratorData.readerIndex(offset);
			 // 更新next元素
			 next = null;
			 next = readNext(null);
		 }
	}

}
