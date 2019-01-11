package lsm.internal;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.FilterPolicy;
import lsm.base.ByteBufUtils;

/**
 * filter数据格式
 * | bitset字节 | bitset实际大小 |
 * 
 * m	bit数组的宽度（bit数）
 * n	加入其中的key的数量
 * k	使用的hash函数的个数
 * f	False Positive的比率
 * @author bird
 *
 */
public class BloomFilter implements FilterPolicy{
	private static final Logger LOG = LoggerFactory.getLogger(BloomFilter.class);
	private static final double BLOOM_FILTER_CONSTANTS = 0.6185;
	/**
	 * bloom filter错误率
	 */
	private static final double FALSE_POSITIVE = 0.03;
	/**
	 * 使用的hash函数的个数,k = -ln(f) / ln(2) 
	 */
	private final static int k = 5;
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
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(keys.length > 0);
		return createFilter(Lists.asList(keys[0], keys));
	}
	@Override
	public ByteBuf createFilter(List<ByteBuf> keys) {
		Preconditions.checkNotNull(keys);
		Preconditions.checkArgument(keys.size() > 0);
		// 依据公式：
		// n = m ln(0.6185) / ln(f) 
		// m = n * ln(f) / ln(0.6185)
		// 确定比特数
		int bitSetSize = (int) ((double)keys.size() * Math.log(FALSE_POSITIVE) / Math.log(BLOOM_FILTER_CONSTANTS));
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
		// filter信息的末尾是bitset大小信息
		int retSize = filter.readableBytes() - Integer.BYTES;
		int bitsetSize = filter.getInt(filter.readerIndex() + retSize);
		// 实际有效filter信息
		ByteBuf realFilter = filter.slice(filter.readerIndex(), retSize);
		// TODO 可优化. 建立byte数组，盛放key和附加k
		byte[] data = new byte[key.readableBytes() + Integer.BYTES];
		// 映射k次
		for (int x = 0; x < k; x++) {
			// 将key中的字节读入数组
			key.slice().readBytes(data, 0, key.readableBytes());
			// 将k组装到key的末尾
			data[key.readableBytes()] = ((k >> 24) & 0xff);
			data[key.readableBytes() + 1] = ((k >> 16) & 0xff);
			data[key.readableBytes() + 2] = ((k >> 8) & 0xff);
			data[key.readableBytes() + 3] = (k & 0xff);
			// 生成hash数据
			long hash = createHash(data);
			hash = hash % (long) bitsetSize;
			// 根据hash数据和filter信息，替代bitset来检验key的存在
			int index = Math.abs((int) hash);
			int i = index / 8;
			int j = 7 - index % 8;
			boolean ret = (realFilter.getByte(i) & (1 << j)) >> j == 1 ? true : false;
			if (!ret)
				return false;
		}
		return true;
	}
	
	/**
	 * 将key添加到bitset中
	 * @param bitset
	 * @param key
	 * @param bitsetSize bitset实际大小
	 */
	private void add(BitSet bitset, ByteBuf key, int bitsetSize) {
		// TODO 可优化
		byte[] data = new byte[key.readableBytes() + Integer.BYTES];
		// 映射k次
		for (int x = 0; x < k; x++) {
			// 将key中的字节读入数组
			key.slice().readBytes(data, 0, key.readableBytes());
			// 将k组装到key的末尾
			data[key.readableBytes()] = ((k >> 24) & 0xff);
			data[key.readableBytes() + 1] = ((k >> 16) & 0xff);
			data[key.readableBytes() + 2] = ((k >> 8) & 0xff);
			data[key.readableBytes() + 3] = (k & 0xff);
			// 生成hash数据
			long hash = createHash(data);
			hash = hash % (long) bitsetSize;
			// 将hash数据加入到bitset中
			bitset.set(Math.abs((int) hash), true);
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
//		if (bitSet.size() != bitSetSize) {
//			LOG.debug("bitSet.size() = " + bitSet.size());
//			LOG.debug("bitSetSize = " + bitSetSize);
//		}
		// 计算ret所需大小
		int retSize = (bitSet.size() / 8) + Integer.BYTES;
		LOG.debug("retSize = " + retSize);
		// 分配bytebuf
		ByteBuf ret = PooledByteBufAllocator.DEFAULT.buffer(retSize);
		// 先将bytebuf填充满
		ret.writerIndex(retSize);
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
