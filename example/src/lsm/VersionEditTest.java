package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.ByteBufUtils;
import lsm.base.FileMetaData;
import lsm.base.InternalKey;
import lsm.base.Options;
import lsm.internal.VersionEdit;

public class VersionEditTest {

	public static void main(String[] args) {
		VersionEdit edit = new VersionEdit();
		InternalKey key1 = new InternalKey(ByteBufUtils.str2Buf("key1"), 10005L);
		InternalKey key2 = new InternalKey(ByteBufUtils.str2Buf("key2"), 10004L);
		InternalKey key3 = new InternalKey(ByteBufUtils.str2Buf("key3"), 10003L);
		InternalKey key4 = new InternalKey(ByteBufUtils.str2Buf("key4"), 10002L);
		InternalKey key5 = new InternalKey(ByteBufUtils.str2Buf("key5"), 10001L);
		InternalKey key6 = new InternalKey(ByteBufUtils.str2Buf("key6"), 10000L);
		FileMetaData file1 = new FileMetaData(999L, 1024, key3, key4);
		FileMetaData file2 = new FileMetaData(1000L, 4096, key5, key6);
		edit.setComparatorName(Options.INTERNAL_KEY_COMPARATOR_NAME);
		edit.setLogNumber(999L);
		edit.setNextFileNumber(1001L);
		edit.setLastSequenceNumber(123456L);
		edit.setCompactPointer(0, key1);
		edit.setCompactPointer(1, key2);
		edit.deleteFile(0, 998L);
		edit.deleteFile(1, 997L);
		edit.deleteFile(1, 996L);
		edit.deleteFile(1, 995L);
		edit.addFile(3, file1);
		edit.addFile(4, file2);
		
		ByteBuf record = edit.encode();
		VersionEdit edit1 = new VersionEdit(record);
		System.out.println(edit1.getComparatorName());
		System.out.println(edit1.getLogNumber());
		System.out.println(edit1.getNextFileNumber());
		System.out.println(edit1.getLastSequenceNumber());
		System.out.println(edit1.getCompactPointers());
		System.out.println(edit1.getDeletedFiles());
		System.out.println(edit1.getNewFiles());

	}

}
