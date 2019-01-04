package lsm.internal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.jboss.netty.util.internal.ByteBufferUtil;

import io.netty.buffer.ByteBuf;
import lsm.BlockBuilder;
import lsm.base.ByteBufUtils;

/**
 * SSTable block的典型结构是
 * |      record      | 重启点机制优化后的数据记录
 * |   restarts(4B)   | 重启点位置信息
 * |restart counts(4B)| 重启点数目
 * 
 * 每条record的典型结构
 * | key共享长度(4B) | key非共享长度(4B) | value长度(4B) | key非共享内容 | value |
 * @author bird
 *
 */
public class BlockBuilderImpl implements BlockBuilder{
	/**
     * interval数目个record进行公共前缀压缩（重启点机制）
     */
    private final int interval;

    private final List<Integer> restartPoints = new ArrayList<>();
    /**
     * 当前key数目
     */
    private int count;
    
    private ByteBuf lastKey;
    
    private ByteBuf block;
    
	public BlockBuilderImpl(int interval) {
		this.interval = interval;
		// TODO
		this.block = null;
	}
    
	@Override
	public void add(ByteBuf key, ByteBuf value) {
		// 判断是否是重启点
		if(count % interval == 0) {
			// 如果是重启点，记录重启点位置
			restartPoints.add(block.writerIndex());
			// 记录现场
			ByteBufUtils.markIndex(key);
			// 写record
			writeRecord(key.readableBytes(), 0, value.readableBytes(), key, value);
			// 恢复
			ByteBufUtils.resetIndex(key);

		}
		else {
			// 如果不是重启点，计算共享长度，非共享长度
			int sharedLen = sharedLen(lastKey, key);
			int nonSharedLen = key.readableBytes() - sharedLen;
			// 记录现场
			ByteBufUtils.markIndex(key);
			// key调整读指针
			key.readerIndex(key.readerIndex() + sharedLen);
			// 写record
			writeRecord(sharedLen, nonSharedLen, value.readableBytes(), key, value);
			// 恢复
			ByteBufUtils.resetIndex(key);
		}
		// 更新lastkey
		lastKey = key;
		// 更新count数目
		count++;
	}
	
	@Override
	public void finish() {
		// 写入重启点位置
		restartPoints.forEach(i -> {
			block.writeInt(i);
		});
		// 写入重启点数目
		block.writeInt(restartPoints.size());
		// TODO 写入block trailer信息
	}
	
	/**
	 * 按照record的规则，写record到block中
	 * @param sharedLen
	 * @param nonSharedLen
	 * @param valueLen
	 * @param key
	 * @param value
	 */
	private void writeRecord(int sharedLen, int nonSharedLen, int valueLen, ByteBuf key, ByteBuf value) {
		// 写入共享长度，非共享长度，value长度
		block.writeInt(sharedLen);
		block.writeInt(nonSharedLen);
		block.writeInt(valueLen);
		// 写入key非共享内容
		block.writeBytes(key);
		// 写入value
		block.writeBytes(value);
	}
	
	/**
	 * 计算两个key的公共长度
	 * @param lastKey
	 * @param key
	 * @return
	 */
	private int sharedLen(ByteBuf lastKey, ByteBuf key) {
		ByteBufUtils.markIndex(key);
		ByteBufUtils.markIndex(lastKey);
		int minLen = Math.min(lastKey.readableBytes(), key.readableBytes());
		int len = 0;
		// TODO 检验
		while(minLen-- >= 0) {
			if(lastKey.readByte() == key.readByte()) {
				len++;
			}
			else {
				ByteBufUtils.resetIndex(key);
				ByteBufUtils.resetIndex(lastKey);
				return len;
			}
		}
		ByteBufUtils.resetIndex(key);
		ByteBufUtils.resetIndex(lastKey);
		return len;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public int size() {
		return block.readableBytes();
	}


}
