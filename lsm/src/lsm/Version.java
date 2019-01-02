package lsm;

import lsm.base.Compaction;

public interface Version {
	public Compaction getCompactionInfo();
}
