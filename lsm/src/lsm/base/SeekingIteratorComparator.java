package lsm.base;

import java.util.Comparator;

import io.netty.buffer.ByteBuf;
import lsm.SeekingIterator;

/**
 * 用于比较seekingIterator间的大小，依据迭代器当前位置数据大小
 * @author bird
 *
 */
public class SeekingIteratorComparator implements Comparator<SeekingIterator<ByteBuf, ByteBuf>>{
	/**
	 * internalKey的比较器
	 */
	public Comparator<InternalKey> internalKeyComparator;
	
	public SeekingIteratorComparator(Comparator<InternalKey> internalKeyComparator) {
		this.internalKeyComparator = internalKeyComparator;
	}
	
	@Override
	public int compare(SeekingIterator<ByteBuf, ByteBuf> left, SeekingIterator<ByteBuf, ByteBuf> right) {
		// 获取左边和右边的迭代器的当前位置元素，再根据它获取internalKey
		InternalKey leftKey = new InternalKey(left.peek().getKey());
		InternalKey rightKey = new InternalKey(right.peek().getKey());
		// 按照InternalKey的大小，升序排序
		return internalKeyComparator.compare(leftKey, rightKey);
	}


	
}
