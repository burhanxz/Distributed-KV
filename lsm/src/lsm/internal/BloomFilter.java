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
		// 将所有key投影到到bitset中去
		for(ByteBuf key : keys) {
			add(bitset, key, bitSetSize);
		}
		// 根据bitset生成bytebuf作为最终结果
		ByteBuf ret = bitSetToResult(bitset, bitSetSize);
		return ret;
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
		// 记录当前位置
		int writerIndex = key.writerIndex();
		// TODO 可优化
		byte[] data = new byte[key.readableBytes() + Integer.BYTES];
		// 映射k次
		for (int x = 0; x < k; x++) {
			ByteBufUtils.markIndex(key);
			// 将k组装到key的末尾
			key.setInt(writerIndex, k);
			// 将key中的字节读入数组
			key.readBytes(data);
			// 生成hash数据
			long hash = createHash(data);
			hash = hash % (long) bitsetSize;
			// 将hash数据加入到bitset中
			bitset.set(Math.abs((int) hash), true);
			// 恢复key
			ByteBufUtils.resetIndex(key);
		}
		
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
	
	/**
	 * MD5算法生成hash值
	 * @param data
	 * @return
	 */
	public long createHash(byte[] data) {
		long h = 0;
		byte[] res;
		synchronized (digestFunction) {
			res = digestFunction.digest(data);
		}
		for (int i = 0; i < 4; i++) {
			h <<= 8;
			h |= ((int) res[i]) & 0xFF;
		}
		return h;
	}

}
