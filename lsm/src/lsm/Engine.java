package lsm;
// TODO
public interface Engine {
	public void put();
	
	public void delete();
	
	public byte[] get();
}
