package lsm.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.FilterPolicy;
import lsm.base.ByteBufUtils;

/**
 * filter数据格式
 * | bitset字节 | bitset实际大小 |
 * @author bird
 *
 */
public class BloomFilter implements FilterPolicy{
	private final static int k = 7;
	private final static double bitPerKey = 10;
	/**
	 *  在大多数情况下，MD5提供了较好的散列精确度。如有必要，可以换成 SHA1算法
	 */
	private final static String hashName = "MD5";
	/**
	 * MessageDigest类用于为应用程序提供信息摘要算法的功能，如 MD5 或 SHA 算法
	 */
	private static MessageDigest digestFunction;
	
	static {
		// 初始化 MessageDigest 的摘要算法对象
		MessageDigest tmp;
		try {
			tmp = java.security.MessageDigest.getInstance(hashName);
		} catch (NoSuchAlgorithmException e) {
			tmp = null;
		}
		digestFunction = tmp;
	}
	
	@Override
	public ByteBuf createFilter(ByteBuf[] keys) {
		// 确定bitset大小
		int bitSetSize = (int) Math.ceil(bitPerKey * keys.length);
		// 新建bitSet
		BitSet bitset = new BitSet(bitSetSize);
		
		return null;
	}

	@Override
	public boolean keyMayMatch(ByteBuf key, ByteBuf filter) {
		// TODO Auto-generated method stub
		return false;
	}
	
	/**
	 * 将key添加到bitset中
	 * @param bitset
	 * @param key
	 * @param bitsetSize bitset实际大小
	 */
	private void add(BitSet bitset, ByteBuf key, int bitsetSize) {
		ByteBufUtils.markIndex(key);
		int writerIndex = key.writerIndex();
		// 映射k次
		for (int x = 0; x < k; x++) {
			// 将k组装到key的末尾
			key.setInt(writerIndex, k);
		}
		ByteBufUtils.resetIndex(key);
	}
	
	/**
	 * 由bitset生成对应的bytebuf
	 * @param bitSet 
	 * @param bitSetSize bitset实际大小
	 * @return bytebuf
	 */
	private ByteBuf bitSetToResult(BitSet bitSet, int bitSetSize) {
		// 测试
		if (bitSet.size() != bitSetSize) {
			System.out.println("bitSet.size() = " + bitSet.size());
			System.out.println("bitSetSize = " + bitSetSize);
		}
		// 计算ret所需大小
		int retSize = (bitSet.size() / 8) + Integer.BYTES;
		// 分配bytebuf
		ByteBuf ret = PooledByteBufAllocator.DEFAULT.buffer(retSize);
		// 将bitset中的数据映射到bytebuf中
		for (int i = 0; i < bitSet.size(); i++) {
			int index = i / 8;
			int offset = 7 - i % 8;
			byte b = ret.getByte(index);
			b |= (bitSet.get(i) ? 1 : 0) << offset;
			ret.setByte(index, b);
		}
		// 将bitset大小信息添加到最后
		ret.setInt((bitSet.size() / 8), bitSetSize);
		return ret;
	}
	
	
}
