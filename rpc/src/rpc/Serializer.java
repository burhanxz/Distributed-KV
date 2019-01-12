package rpc;

public interface Serializer {
	/**
	 * 序列化对象
	 * @param obj 被序列化的对象
	 * @return 序列化得到的字节数组
	 */
	public <T> byte[] serialize(T obj);
	/**
	 * 反序列化数据
	 * @param data 待反序列化数据
	 * @param clazz 指定对象类型
	 * @return 反序列化得到的对象
	 */
	public <T> T deserialize(byte[] data, Class<T> clazz);
}
