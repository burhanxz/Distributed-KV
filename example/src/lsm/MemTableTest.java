package lsm;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lsm.MemTable;
import lsm.base.ByteBufUtils;
import lsm.base.InternalKey;
import lsm.internal.MemTableImpl;

public class MemTableTest {
	private final static String VALUE = "this_is_value";
	public static void main(String[] args) {
		MemTable mem = new MemTableImpl();
		AtomicLong seq = new AtomicLong(0l);
		//测试数据
		List<String> keys = Datas.getKeys();
		List<String> values = Datas.getValues();
		for(int i = 0; i < 10000; i++) {
			String key = keys.get(i);
			String value = values.get(i); 
			mem.add(seq.getAndIncrement(), InternalKey.InternalKeyType.ADD, Unpooled.wrappedBuffer(key.getBytes(StandardCharsets.UTF_8)), Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8)));
		}

		Iterator<Entry<InternalKey, ByteBuf>> iter = mem.iterator();
		while(iter.hasNext()) {
			Entry<InternalKey, ByteBuf> entry = iter.next();
			String key = ByteBufUtils.buf2Str(entry.getKey().getUserKey());
			String value = ByteBufUtils.buf2Str(entry.getValue());
			System.out.println(key + ", " + value);
		}
		System.out.println(mem.size());
		
	}

}
