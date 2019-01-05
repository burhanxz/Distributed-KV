package lsm.internal;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.buffer.ByteBuf;
import lsm.Level;

import lsm.Version;
import lsm.VersionSet;
import lsm.base.FileMetaData;

public class VersionImpl implements Version{
	private final AtomicInteger retained = new AtomicInteger(1);
//	private final VersionSet versionSet;
//	private final Level0 level0;
//	private final List<Level> levels;

	// move these mutable fields somewhere else

	// 需要进行合并的level
	private int compactionLevel;
	// 当score>=1时，也需要进行合并
	private double compactionScore;
	@Override
	public int pickLevelForMemTableOutput(ByteBuf smallest, ByteBuf largest) {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public double getCompactionScore() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setCompactionScore(double score) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getCompactionLevel() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void setCompactionLevel(int level) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void retain() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
	

}
