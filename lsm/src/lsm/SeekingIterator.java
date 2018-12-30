package lsm;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * @author bird
 * 增强的Iterator
 * @param <K> 键类型
 * @param <V> 值类型
 */
public interface SeekingIterator<K,V> extends Iterator<Entry<K,V>>{
		
	/**
	 * 取出当前指针数据但是不移动指针
	 * @return 当前指针位置数据
	 */
	public Entry<K,V> peek();
	
	/**
	 * 指针移动到key对应的位置
	 * @param key 键
	 * @return
	 */
	public void seek(K key);
	
	/**
	 * 寻找第一个位置的数据
	 * @return 第一个位置的数据
	 */
	public Entry<K,V> seekToFirst();

	@Override
	public void remove();
}
