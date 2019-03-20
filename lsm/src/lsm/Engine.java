package lsm;

import java.io.IOException;

// TODO
public interface Engine {
	public void put(byte[] key, byte[] value) throws IOException;
	
//	public void delete();
//	
//	public byte[] get();
}
