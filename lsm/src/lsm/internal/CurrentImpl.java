package lsm.internal;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import com.google.common.base.Preconditions;
import com.google.common.io.Files;

import lsm.Current;
import lsm.base.FileUtils;

public class CurrentImpl implements Current {
	/**
	 * current所属目录
	 */
	private final File databaseDir;
	/**
	 * current绑定的文件
	 */
	private File currentFile;
	public CurrentImpl(File databaseDir) {
		this.databaseDir = databaseDir;
		this.currentFile = new File(databaseDir, FileUtils.currentFileName());
	}
	@Override
	public void setManifest(long menifestFileNumber) throws IOException {
		Preconditions.checkArgument(menifestFileNumber >= 1);
		// 如果current文件不存在，新建一个
		if(!currentFile.exists()) {
			currentFile.createNewFile();
		}
        // 获取manifest文件名
    	String manifest = FileUtils.manifestFileName(menifestFileNumber);
        // 将manifest文件信息写入current文件
        ByteBuffer buffer = ByteBuffer.wrap((manifest + "\n").getBytes(StandardCharsets.UTF_8));
        try(RandomAccessFile raf = new RandomAccessFile(currentFile, "rw");
        		FileChannel channel = raf.getChannel()){
        	channel.write(buffer);
        	channel.force(false);
        }
	}

	@Override
	public String getManifest() throws IOException {
		Preconditions.checkState(isAvailable());
		// 获取current文件内容
		String manifest = Files.toString(currentFile, UTF_8);
		Preconditions.checkState(!manifest.isEmpty());
		Preconditions.checkArgument(manifest.endsWith("\n"));
		// 生成当前manifest的文件名
		manifest = manifest.substring(0, manifest.length() - 1);
		return manifest;
	}

	@Override
	public boolean isAvailable() {
		return currentFile.exists() && currentFile.canRead();
	}

}
