package core;

public interface Pipeline {
	
	public boolean addValve();
	
	public Valve removeValve();

	public Valve firstValve();
	
}
