package rpc.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WeakValueHashMap<K, V> {
	private Map<K, WeakReference<V>> map = new ConcurrentHashMap<>();

	private ReferenceQueue<V> referenceQueue = new ReferenceQueue<>();

	public WeakValueHashMap() {
		new Thread(() -> {
			try {
				WeakReference<V> wr = (WeakReference<V>) referenceQueue.remove();
				
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}).start();
	}

	public V get(K k) {

		return null;
	}

}
