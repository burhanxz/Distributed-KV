package lsm;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import lsm.VersionSet;
import lsm.base.ByteBufUtils;
import lsm.base.Compaction;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;
import lsm.base.Options;
import lsm.internal.VersionEdit;
import lsm.internal.VersionSetImpl;

public class VersionTest {
	private static final String dir = "/home/bird/eclipse-workspace/distributed-kv/example/src/lsm";
	private static final long FILE_SIZE = 1 << 20;
	private static final AtomicLong seq = new AtomicLong(0L);
	public static void main(String[] args) throws IOException {
		// 初始化versionSet
		VersionSet versionSet = new VersionSetImpl(new File(dir), 1000);
		versionSet.recover();
		// 初始化log number
		final long logNumber = versionSet.getNextFileNumber();
		// 第一个文件
		FileMetaData f1 = new FileMetaData(versionSet.getNextFileNumber(), FILE_SIZE, key("a"), key("c"));
		VersionEdit e1 = getEdit(versionSet, logNumber);
		e1.addFile(0, f1);
		versionSet.logAndApply(e1);
		System.out.println(versionSet.getCurrent());
		// 2
		FileMetaData f2 = new FileMetaData(versionSet.getNextFileNumber(), FILE_SIZE, key("b"), key("d"));
		VersionEdit e2 = getEdit(versionSet, logNumber);
		e2.addFile(0, f2);
		versionSet.logAndApply(e2);
		System.out.println(versionSet.getCurrent());
		// 3
		FileMetaData f3 = new FileMetaData(versionSet.getNextFileNumber(), FILE_SIZE, key("e"), key("f"));
		VersionEdit e3 = getEdit(versionSet, logNumber);
		e3.addFile(0, f3);
		versionSet.logAndApply(e3);
		System.out.println(versionSet.getCurrent());
		// 4
		FileMetaData f4 = new FileMetaData(versionSet.getNextFileNumber(), FILE_SIZE, key("g"), key("h"));
		VersionEdit e4 = getEdit(versionSet, logNumber);
		e4.addFile(0, f4);
		versionSet.logAndApply(e4);
		System.out.println(versionSet.getCurrent());
		
		if(versionSet.needsCompaction()) {
			Compaction c = versionSet.pickCompaction();
			System.out.println("getLevel() = " + c.getLevel());
			System.out.println("getLevelInputs" + c.getLevelInputs());
			System.out.println("getLevelUpInputs" + c.getLevelUpInputs());
		}
		
}
	
	private static VersionEdit getEdit(VersionSet versionSet, long logNumber) {
		// 产生edit
		VersionEdit edit = new VersionEdit();
		edit.setComparatorName(Options.INTERNAL_KEY_COMPARATOR_NAME);
		edit.setLogNumber(logNumber);
//		edit.setNextFileNumber(versionSet.getNextFileNumber());
		edit.setLastSequenceNumber(seq.get());
		return edit;
	}
	
	private static InternalKey key(String str) {
		InternalKey key = new InternalKey(ByteBufUtils.str2Buf(str), seq.getAndIncrement());
		return key;
	}
}
