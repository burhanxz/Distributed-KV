package lsm.internal;

import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lsm.FilterPolicy;
import lsm.MetaBlockBuilder;

public class MetaBlockBuilderImpl implements MetaBlockBuilder{
	/**
	 * 以此为幂，设置1 << kFilterBaseLg KB生成一个meta block
	 */
	private final int kFilterBaseLg;
	/**
	 * 过滤器
	 */
	private final FilterPolicy filterPolicy;
	/**
	 * 存放所有filter
	 */
	private ByteBuf result;
	/**
	 * 存放所有filter的偏置
	 */
	private List<Integer> filterOffsets;
	/**
	 * 暂存所有key，生成filter后清空
	 */
	private List<ByteBuf> keys;
	
	public MetaBlockBuilderImpl(int kFilterBaseLg, FilterPolicy filterPolicy) {
		this.filterPolicy = filterPolicy;
		this.kFilterBaseLg = kFilterBaseLg;
		result = PooledByteBufAllocator.DEFAULT.buffer();
		filterOffsets = new ArrayList<>();
		keys = new ArrayList<>();
	}

	@Override
	public void startBlock(int blockOffset) {
		// 计算在此block offset时，理应存在多少个filter
		int filters = blockOffset >> kFilterBaseLg;
		// 如果理论filter数量大于实际filter数量，则生成新的filter
		while(filters > filterOffsets.size()) {
			generateFilter();
		}
	}

	@Override
	public void addKey(ByteBuf key) {
		keys.add(key);
	}

	@Override
	public ByteBuf finish() {
		int filtersSize = result.readableBytes();
		// 将filter offsets信息加入result
		filterOffsets.forEach(i -> {
			result.writeInt(i);
		});
		// 将filter总大小信息加入result
		result.writeInt(filtersSize);
		// 将kFilterBaseLg信息加入到result
		result.writeInt(kFilterBaseLg);
		return result;
	}

	/**
	 * 产生filter数据，将filter偏置写入filter offset
	 */
	private void generateFilter() {
		// 如果keys是空的，则将result的大小直接放入filter offsets
		if(keys.isEmpty()) {
			filterOffsets.add(result.readableBytes());
		}
		else {
			// 将keys提取出来，生成filter数据
			ByteBuf filter = filterPolicy.createFilter((ByteBuf[])keys.toArray());
			// 将filter加入到result中
			result.writeBytes(filter);
			// 将当前result大小加入到filter offsets中
			filterOffsets.add(result.readableBytes());
			// 清空keys
			keys.clear();
		}
	}
}
