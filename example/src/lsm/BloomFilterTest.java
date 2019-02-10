package lsm;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lsm.internal.BloomFilter;

public class BloomFilterTest {
	private static final int count = 1000;
	private static final List<ByteBuf> data = new ArrayList<>();
	static {
		Random random = new Random();
		for(int i = 0; i < count; i++) {
			long x = random.nextLong();
			data.add(Unpooled.wrappedBuffer(longToBytes(x)));
		}
	}
	public static void main(String[] args) {
		BloomFilter bf = new BloomFilter();

		ByteBuf filter = bf.createFilter(data);
		for(int i = 0; i < count; i++) {
			Preconditions.checkState(bf.keyMayMatch(data.get(i), filter));
		}
		System.out.println("正确数据全部存在");
		
		int sum = 0;
		for(int i = 0; i < count; i++) {
			boolean ret = bf.keyMayMatch(data.get(i).setByte(0, 0), filter);
			if(ret) {
				sum++;
			}
		}
		System.out.println("错误率: " + (sum * 1.0 / count));
	}
	
	public static byte[] longToBytes(long values) {
		byte[] buffer = new byte[8];
		for (int i = 0; i < 8; i++) {
			int offset = 64 - (i + 1) * 8;
			buffer[i] = (byte) ((values >> offset) & 0xff);
		}
		return buffer;
	}

}
