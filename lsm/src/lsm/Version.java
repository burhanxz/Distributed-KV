package lsm;

public interface Version {
	public int numberOfLevels();
	
	public int numberOfFilesInLevel(int level);
	
	
	
	public int getCompactionLevel();
	
	public void setCompactionLevel(int compactionLevel);
	
	public double getCompactionScore();
	
	public void setCompactionScore(double compactionScore);
}
