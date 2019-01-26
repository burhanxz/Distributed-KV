package lsm.base;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import com.google.common.base.Preconditions;
import sun.misc.Cleaner;

@SuppressWarnings("restriction")
public class MmapReleaseUtil {
	/**
	 * 用于获取Cleaner类对象的方法
	 */
	private static Method cleanerMethod;
	static {
		try {
			// 获取方法和使用权限
			cleanerMethod = MappedByteBuffer.class.getMethod("cleaner", new Class[0]);
			cleanerMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 释放mmap内存
	 * @param buffer
	 * @throws Exception
	 */
	public static void clean(MappedByteBuffer buffer) {
		Preconditions.checkNotNull(buffer);
		// 获取buffer绑定的cleaner类对象
		sun.misc.Cleaner cleaner = null;
		try {
			cleaner = (Cleaner) cleanerMethod.invoke(buffer, new Object[0]);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		// 释放mmap内存
		cleaner.clean();
	}
}
