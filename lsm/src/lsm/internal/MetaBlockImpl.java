package lsm.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.FilterPolicy;
import lsm.MetaBlock;

public class MetaBlockImpl implements MetaBlock{
	private static final Logger LOG = LoggerFactory.getLogger(MetaBlockImpl.class);
	/**
	 * 过滤器
	 */
	private final FilterPolicy filterPolicy;
	/**
	 * 完整meta block数据
	 */
	private final ByteBuf result;
	// result切分后得到的xinx
	/**
	 * 实际有效filter部分
	 */
	private ByteBuf filters;
	/**
	 * filter offset数组
	 */
	private int[] filterOffsets;
	/**
	 * 有效filter部分的总大小
	 */
	private int sum;
	/**
	 * 基数
	 */
	private int kFilterBaseLg;

	public MetaBlockImpl(FilterPolicy filterPolicy, ByteBuf result) {
		Preconditions.checkNotNull(filterPolicy);
		Preconditions.checkNotNull(result);
		this.filterPolicy = filterPolicy;
		this.result = result;
		// 获取kFilterBase
		kFilterBaseLg = result.getInt(result.readerIndex() + result.readableBytes() - Integer.BYTES);
		// 获取sum,filters的总大小
		sum = result.getInt(result.readerIndex() + result.readableBytes() - 2 * Integer.BYTES);
		// 切片得到filters
		filters = result.slice(result.readerIndex(), sum);
		// filter offset信息所占大小
		int filterOffsetSize = result.readableBytes() - 2 * Integer.BYTES - sum;
		// 根据filter offset信息所占大小，得到filter offset的数目，新建filter offset的数组
		filterOffsets = new int[filterOffsetSize / Integer.BYTES];
		// 将filter offset数组设置好
		for(int i = 0; i < filterOffsets.length; i++) {
			filterOffsets[i] = result.getInt(result.readerIndex() + sum + i * Integer.BYTES);
		}
	}
	
	@Override
	public boolean keyMayMatch(int blockOffset, ByteBuf key) {
		Preconditions.checkArgument(blockOffset >= 0);
		Preconditions.checkNotNull(key);
		// 获取有效filter位置
		int n = blockOffset >> kFilterBaseLg;
		// TODO 此处需要验证. 获取有效filter数据
		// filter终止位置
		int filterEndOffset = filterOffsets[n];
		// 寻找第一个不和终止位置相同的filter offset作为起始位置
		int filterStartOffset = 0;
		for(int i = n - 1; i >= 0; i--) {
			if(filterOffsets[i] != filterOffsets[n]) {
				filterStartOffset = filterOffsets[i];
				break;
			}
		}
		// 切片，获取实际filter数据
		ByteBuf filter = filters.slice(filters.readerIndex() + filterStartOffset, filterEndOffset - filterStartOffset);
		// 获取filter中包含的offset信息，做校验
		int offset = filter.slice().getInt(filter.writerIndex() - Integer.BYTES);
		filter = filter.slice(filter.readerIndex(), filter.readableBytes() - Integer.BYTES);
		Preconditions.checkArgument(blockOffset == offset, String.format("blockOffset %d 对应的filter出错, offset应为 %d", blockOffset, offset));
		// 调用过滤器
		boolean exists = filterPolicy.keyMayMatch(key, filter);
		return exists;
	}
	
}
