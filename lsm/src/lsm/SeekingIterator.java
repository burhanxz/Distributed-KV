package lsm;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * 增强的Iterator
 * @author bird
 * 
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
	 * 指针移动到第一个 >= key 的位置
	 * @param key 键
	 * @return
	 */
	public void seek(K key);
	
	/**
	 * 指针移动到第一个位置
	 * @return 
	 */
	public void seekToFirst();
}
