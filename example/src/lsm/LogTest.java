package lsm;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lsm.LogReader;
import lsm.LogWriter;
import lsm.base.ByteBufUtils;
import lsm.base.InternalKey;
import lsm.internal.LogReaderImpl;
import lsm.internal.LogWriterImpl;

public class LogTest {
	private static final String dir = "/home/bird/eclipse-workspace/distributed-kv/example/src/lsm";
	public static void main(String[] args) throws IOException {
		LogWriter writer = new LogWriterImpl(new File(dir), 3, false);
		AtomicLong seq = new AtomicLong(0l);
		//测试数据
		List<String> keys = Datas.getKeys();
		List<String> values = Datas.getValues();
		for(int i = 0; i < 10000; i++) {
			String key = keys.get(i);
			String value = values.get(i);
			InternalKey internalKey = new InternalKey(Unpooled.wrappedBuffer(key.getBytes(StandardCharsets.UTF_8)), seq.getAndIncrement(), InternalKey.InternalKeyType.ADD);
			writer.addRecord(internalKey, Unpooled.wrappedBuffer(value.getBytes(StandardCharsets.UTF_8)), false);
		}
		writer.close();
		// 获取日志
		LogReader reader = new LogReaderImpl(new File(dir), 3, false);
		for(ByteBuf record = reader.readNextRecord(); record != null; record = reader.readNextRecord()) {
			int internalKeySize = record.readInt();
			ByteBuf internalKeyBuf = record.readRetainedSlice(internalKeySize);
			int valueSize = record.readInt();
			ByteBuf valueBuf = record.readRetainedSlice(valueSize);
			Preconditions.checkArgument(record.readableBytes() == 0);
			InternalKey internalKey = InternalKey.decode(internalKeyBuf);
//			System.out.println("internalKeySize = " + internalKeyBuf.readableBytes());
//			System.out.println("valueSize = " + valueBuf.readableBytes());
//			System.out.println(internalKey.getSeq());
			String value = ByteBufUtils.buf2Str(valueBuf);
			String key = ByteBufUtils.buf2Str(internalKey.getUserKey());
			System.out.println(key + " : " + value);
		}
	}

}
