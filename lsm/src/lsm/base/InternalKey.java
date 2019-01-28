package lsm.base;

import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

/**
 * 包含实际key，序列号和类型（删除或增加）
 * 序列化结构:
 * | seq & type (8B) | userKey |
 * @author bird
 *
 */
public class InternalKey{
	/**
	 * 实际输入的key内容
	 */
	private ByteBuf userKey;
	/**
	 * 全局唯一序列号
	 */
	private Long seq;
	/**
	 * flag=false代表删除，flag=true代表添加
	 */
	private InternalKeyType type;
	
	/**
	 * 默认是添加key
	 * @param userKey
	 * @param seq
	 */
	public InternalKey(ByteBuf userKey, long seq) {
		this(userKey, seq, InternalKeyType.ADD);
	}
	
	/**
	 * @param userKey
	 * @param seq
	 * @param flag 设置添加或者删除操作
	 */
	public InternalKey(ByteBuf userKey, long seq, InternalKeyType type) {
		Preconditions.checkNotNull(userKey);
		Preconditions.checkArgument(seq >= 0);
		Preconditions.checkNotNull(type);
		this.userKey = userKey;
		this.seq = seq;
		this.type = type;
	}
	
	/**
	 * 反序列化得到internal key对象,字节数据不包含大小信息
	 * @param byteBuf 字节数据
	 * @return internal key对象
	 */
	public static InternalKey decode(ByteBuf bytebuf) {
		Preconditions.checkNotNull(bytebuf);
		bytebuf = bytebuf.slice();
		// bytebuf前 8B 即为info信息
		long info = bytebuf.readLong();
		// 将读取的info解析成state和seq信息
		int state = (int) (info & 0xff);
		long seq = info >> 8;
		// bytebuf剩余部分即user key
		ByteBuf userKey = bytebuf.slice();
		Preconditions.checkArgument(state == 0 || state == 1);		
		return new InternalKey(userKey, seq, InternalKeyType.getType(state));
	}
	
	/**
	 * 反序列化得到internal key对象,字节数据包含大小信息
	 * @param byteBuf byteBuf 字节数据
	 * @return internal key对象
	 */
	@Deprecated
	public static InternalKey decodeWithPrefix(ByteBuf bytebuf) {
		Preconditions.checkNotNull(bytebuf);
		bytebuf = bytebuf.slice();
		// bytebuf前 8B 即为info信息
		long info = bytebuf.readLong();
		// TODO 将读取的info解析成state和seq信息
		int state = (int) (info & 0xff);
		long seq = info >> 8;
		// bytebuf剩余部分即user key
		ByteBuf userKey = bytebuf.retainedSlice();
		Preconditions.checkArgument(state == 0 || state == 1);
		return new InternalKey(userKey, seq, InternalKeyType.getType(state));
	}
	
	
	
	/**
	 * 序列化信息不包含前缀
	 * @return
	 */
	public ByteBuf encode() {
		ByteBuf buff = PooledByteBufAllocator.DEFAULT.buffer(Long.BYTES + userKey.readableBytes());
		// info存放seq和类型信息，低8位存放类型信息
		long info = seq << 8 | (type.getState() & 0xff);
		buff.writeLong(info);
		// 写入user key
		buff.writeBytes(userKey.slice());
		return buff;
	}
	
	/**
	 * 在序列化数据开始的地方写入大小信息作为前缀
	 * @return
	 */
	@Deprecated
	public ByteBuf encodeWithPrefix() {
		ByteBuf buff = PooledByteBufAllocator.DEFAULT.buffer(Integer.BYTES + userKey.readableBytes() + Long.BYTES);
		// 首先写入userkey和info总大小来作为前缀
		buff.writeInt(userKey.readableBytes() + Long.BYTES);
		buff.writeBytes(userKey.array(), userKey.readerIndex(), userKey.readableBytes());
		// info存放seq和类型信息，低8位存放类型信息
		long info = seq << 8 | (byte) type.getState();
		buff.writeLong(info);
		return buff;
	}
	/**
	 * 计算internalKey的大小
	 * @return
	 */
	public int size() {
		return userKey.readableBytes() + Long.BYTES;
	}
	
	/**
	 * 获取实际key
	 * @return
	 */
	public ByteBuf getUserKey() {
		return userKey;
	}
	/**
	 * 获取序列号
	 * @return
	 */
	public long getSeq() {
		return seq;
	}
	/**
	 * 获取internal key 类型
	 * @return 
	 */
	public InternalKeyType getType() {
		return type;
	}
	
	/**
	 * 代表internal key的类型，有delete和add
	 * @author bird
	 *
	 */
	public enum InternalKeyType{
		DELETE(0), ADD(1);
		InternalKeyType(int state){
			this.state = state;
		}
		/**
		 * 状态码
		 */
		int state;
		/**
		 * 返回状态码
		 * @return
		 */
		public int getState() {
			return state;
		};
		public static InternalKeyType getType(int state) {
			switch(state) {
			case 0 : return DELETE;
			case 1 : return ADD;
			default : return null;
			}
		}
	}

	@Override
	public String toString() {
		// 方便测试用
		return new StringBuilder().append("internalKey: [ userKey=").append(ByteBufUtils.buf2Str(userKey)).append(", seq=")
				.append(seq).append(", type=").append(type.getState()).append("]").toString();
	}
}
