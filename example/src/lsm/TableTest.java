package lsm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lsm.SSTable;
import lsm.SSTableBuilder;
import lsm.SeekingIterator;
import lsm.base.ByteBufUtils;
import lsm.internal.SSTableBuilderImpl;
import lsm.internal.SSTableImpl;

public class TableTest {
	private static final String dir = "/home/bird/eclipse-workspace/distributed-kv/lsm/src/test";
	private final static String VALUE = "this_is_value";
	public static void main(String[] args) throws IOException {
		//测试数据
		List<String> list = Datas.getKeys();
		// 新建builder
		SSTableBuilder builder = new SSTableBuilderImpl(5, 1 << 11, new File(dir), 2L);
		list.forEach(s ->{
			try {
				builder.add(Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)), Unpooled.wrappedBuffer(VALUE.getBytes()));
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		builder.finish();
		// 读取table
		SSTable table = new SSTableImpl(new File(dir), 2L);
		SeekingIterator<ByteBuf, ByteBuf> iter = table.iterator();
		while(iter.hasNext()) {
			Entry<ByteBuf, ByteBuf> entry = iter.next();
			String key = ByteBufUtils.buf2Str(entry.getKey());
			String value = ByteBufUtils.buf2Str(entry.getValue());
			System.out.println(key + ", " + value);
		}
		table.close();
		// 验证读取的数据和写入的数据是否一致
		SSTable table2 = new SSTableImpl(new File(dir), 2L);
		SeekingIterator<ByteBuf, ByteBuf> iter2 = table2.iterator();
		int index = 0;
		while(iter2.hasNext()) {
			Preconditions.checkState(list.get(index++).equals(ByteBufUtils.buf2Str(iter2.next().getKey())));
		}
		table2.close();
		// 验证数据是否存在
		SSTable table3 = new SSTableImpl(new File(dir), 2L);
		SeekingIterator<ByteBuf, ByteBuf> iter3 = table3.iterator();
		iter3.seek(ByteBufUtils.str2Buf("mnynkot"));
		if(iter3.hasNext()) {
			System.out.println(ByteBufUtils.buf2Str(iter3.next().getKey()) + " : " + ByteBufUtils.buf2Str(iter3.next().getValue()));
		}
		table3.close();
	}
	
	private static void testGetSeparator() {
		ByteBuf buf1 = Unpooled.wrappedBuffer("ahbcbvnrn".getBytes());
		ByteBuf buf2 = Unpooled.wrappedBuffer("ahuglgufby".getBytes());
		ByteBuf sep = SSTableBuilderImpl.getSeparator(buf1, buf2);
		System.out.println(sep.readableBytes());
		System.out.println(ByteBufUtils.buf2Str(sep));
		System.out.println(ByteBufUtils.buf2Str(buf1));
		System.out.println(ByteBufUtils.buf2Str(buf2));
	}

}
