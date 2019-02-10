package lsm;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.log4j.PropertyConfigurator;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lsm.Block;
import lsm.BlockBuilder;
import lsm.SeekingIterator;
import lsm.base.ByteBufUtils;
import lsm.base.InternalKey;
import lsm.internal.BlockBuilderImpl;
import lsm.internal.BlockImpl;

public class BlockTest {
	private final static SortedMap<String, String> data = new TreeMap<>();
	private final static String VALUE = "this_is_value";
	static {
		data.put("hello", "world");
		data.put("hellppp", "world");
		data.put("hellz", "world");
		data.put("hf", "world");
		data.put("iello", "world");
		data.put("isssas", "world");
		data.put("z", "world");
		PropertyConfigurator.configure("src/log4j.properties");
	}
	public static void main(String[] args) {
		// block builder阶段
		BlockBuilder builder = new BlockBuilderImpl(5);
		//测试数据
		List<String> keys = Datas.getKeys();
		List<String> values = Datas.getValues();
		for(int i = 0; i < 10000; i++) {
			String key = keys.get(i);
			String value = values.get(i); 
			builder.add(Unpooled.wrappedBuffer(key.getBytes(StandardCharsets.UTF_8)), Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8)));
		}
		ByteBuf buffer = builder.finish();
		System.out.println("builder count = " + builder.count());
		// block读取阶段
		Block block = new BlockImpl(buffer);
		System.out.println("block data size = " + block.getDataSize());
		System.out.println("block data size = " + block.getFullBlockSize());
		SeekingIterator<ByteBuf, ByteBuf> iter = block.iterator();
		// 测size
		Preconditions.checkState(builder.size() == block.getFullBlockSize());
		// 打印数据
		while(iter.hasNext()) {
			Entry<ByteBuf, ByteBuf> entry = iter.next();
			String key = ByteBufUtils.buf2Str(entry.getKey());
			String value = ByteBufUtils.buf2Str(entry.getValue());
			System.out.println(key + ", " + value);
		}
		// 验证读取的数据和写入的数据是否一致
		SeekingIterator<ByteBuf, ByteBuf> iter2 = block.iterator();
		int index = 0;
		while(iter2.hasNext()) {
			Preconditions.checkState(keys.get(index++).equals(ByteBufUtils.buf2Str(iter2.next().getKey())));
		}
	}

}
