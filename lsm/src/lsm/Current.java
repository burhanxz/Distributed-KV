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
 * TODO
 * current信息
 * @author bird
 *
 */
public interface Current {
	public static final Current INSTANCE = new CurrentImpl(); 
    /**
     * 将manifest文件信息写入current文件
     * @param databaseDir 数据库工作目录
     * @param menifestFileNumber menifest文件编号
     * @return 是否设置成功
     * @throws IOException
     */
    public default boolean setCurrentFile(File databaseDir, long menifestFileNumber) throws IOException
    {
        // 获取manifest文件名
    	String manifest = FileUtils.manifestFileName(menifestFileNumber);
    	// 获取相应的temp文件名
        String temp = FileUtils.tempFileName(menifestFileNumber);
        // 新建temp文件
        File tempFile = new File(databaseDir, temp);
        // 将manifest文件名写入temp文件
        ByteBuffer buffer = ByteBuffer.wrap((manifest + "\n").getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        try(RandomAccessFile raf = new RandomAccessFile(tempFile, "rw");
        		FileChannel channel = raf.getChannel()){
        	channel.write(buffer);
        	channel.force(false);
        }
        // temp文件重命名为current文件
        File to = new File(databaseDir, FileUtils.currentFileName());
        boolean ok = tempFile.renameTo(to);
        // 如果重命名失败，直接将manifest文件信息写入current文件
        if (!ok) {
            tempFile.delete();
            ByteBuffer buffer4To = ByteBuffer.wrap((manifest + "\n").getBytes(StandardCharsets.UTF_8));
            buffer4To.flip();
            try(RandomAccessFile raf = new RandomAccessFile(to, "rw");
            		FileChannel channel = raf.getChannel()){
            	channel.write(buffer4To);
            	channel.force(false);
            }

        }
        return ok;
    }
}
