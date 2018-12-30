package lsm;

import java.util.Map.Entry;

/**
 * @author bird
 * 使用SeekingIterator的Iteratable
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface SeekingIterable<K,V> extends Iterable<Entry<K,V>>{

	@Override
	public SeekingIterator<K,V> iterator();
	
}
