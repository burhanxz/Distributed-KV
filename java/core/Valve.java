package core;

public interface Valve {
	public Valve setNext(Valve valve);
	
	public Valve getNext();
	
	public Object invoke();
}
