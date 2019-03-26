package lsm;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;

import lsm.base.FileUtils;
import lsm.internal.CurrentImpl;

/**
 * current信息
 * @author bird
 *
 */
public interface Current {
    /**
     * 将manifest文件信息写入current文件
     * @param databaseDir 数据库工作目录
     * @throws IOException
     */
	public void setManifest(long menifestFileNumber) throws IOException;
	/**
	 * 获取manifest信息
	 * @return
	 * @throws IOException
	 */
	public String getManifest() throws IOException;
	/**
	 * 检查current文件是否存在
	 * @return
	 */
	public boolean isAvailable();
}
