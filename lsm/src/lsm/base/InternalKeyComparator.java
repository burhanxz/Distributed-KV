package lsm.base;

import java.util.Comparator;

import com.google.common.collect.ComparisonChain;

/**
 * InternalKey的比较器
 * @author bird
 *
 */
public class InternalKeyComparator implements Comparator<InternalKey>{

	@Override
	public int compare(InternalKey left, InternalKey right) {
		// 先比较bytebuf字节数据，再比较序列号大小
		return ComparisonChain.start().compare(left.getUserKey(), right.getUserKey())
			.compare(left.getSeq(), right.getSeq()).result();
	}

}
