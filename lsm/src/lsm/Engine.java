package lsm;

import java.io.IOException;

/**
 * 数据库引擎接口
 * @author bird
 *
 */
public interface Engine {
	/**
	 * 写入数据
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void put(byte[] key, byte[] value) throws IOException;
	
//	public void delete();
//	
	/**
	 * 读取数据
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public byte[] get(byte[] key) throws Exception;
}
