package lsm.internal;

import lsm.Version;
import lsm.VersionSet;
import lsm.base.Compaction;

public class VersionSetImpl implements VersionSet{

	@Override
	public boolean needsCompaction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Compaction pickCompaction() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logAndApply(VersionEdit edit) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public long getLastSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getNextFileNumber() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Version getCurrent() {
		// TODO Auto-generated method stub
		return null;
	}

}
