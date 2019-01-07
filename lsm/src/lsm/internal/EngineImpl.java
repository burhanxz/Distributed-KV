package lsm.internal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import com.google.common.base.Preconditions;

import io.netty.buffer.ByteBuf;
import lsm.MemTable;
import lsm.SSTable;
import lsm.SSTableBuilder;
import lsm.SeekingIterator;
import lsm.TableCache;
import lsm.Version;
import lsm.VersionSet;
import lsm.base.Compaction;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;
import lsm.base.InternalKey;
import lsm.base.Options;
import lsm.base.SeekingIteratorComparator;

public class EngineImpl {
	/**
	 * 引擎基本配置
	 */
	private final Options options;

	/**
	 * 引擎工作目录
	 */
	private final File databaseDir;
	private final VersionSet versions;
	private final TableCache tableCache;
	/**
	 * 指示是否关闭
	 */
	private final AtomicBoolean shuttingDown = new AtomicBoolean();
	/**
	 * 写入wal
	 */
	private LogWriter log;
	private MemTable memTable;
	private MemTable immutableMemTable;
	/**
	 * key的比较器
	 */
	private final Comparator<InternalKey> internalKeyComparator;
	// 执行compaction的线程
	private final ExecutorService compactionExecutor;
	// 后台compaction异步操作
	private Future<?> backgroundCompaction;
	// 用作compaction的锁
	private final ReentrantLock compactionLock = new ReentrantLock();
	// 用作compaction的等待和唤醒
	private final Condition backgroundCondition = compactionLock.newCondition();
	/**
	 * 一些待处理的无用文件，可能需要删除
	 */
	private final List<Long> pendingFileNumbers = new ArrayList<>();
	public EngineImpl(Options options, File databaseDir) {
		// TODO
		this.options = options;
		this.databaseDir = databaseDir;
		this.versions = null;
		this.tableCache = null;
		this.internalKeyComparator = null;
		this.compactionExecutor = null;
	}

	/**
	 * 尝试触发compact
	 */
	private void maybeCompaction() {
		// 不应compact的判断
		boolean shouldNotStartCompact = backgroundCompaction != null // 上次后台合并未结束
				|| shuttingDown.get() // 系统关闭
				|| (immutableMemTable == null && !versions.needsCompaction());// 在内存没有数据需要序列化的情况下，versionSet中未显示需要compact
		if (!shouldNotStartCompact) {
			// 启动compact线程
			// 启动后台compaction线程
			backgroundCompaction = compactionExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					compactionLock.lock();
					try {
						// 执行compaction
						backgroundCompaction();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						// 再次触发compact
						try {
							maybeCompaction();
						} finally {
							// 释放锁，唤醒等待compaction的线程
							try {
								backgroundCondition.signalAll();
							} finally {
								compactionLock.unlock();
							}
						}
					}
					return null;
				}
			});
		}
	}

	/**
	 * 选择执行minor compaction或者major compaction
	 * @throws IOException
	 */
	private void backgroundCompaction() throws IOException {
		// 尝试序列化memtable，优先级最高
		serializeMemTable();
		// 获取compaction信息
		Compaction compaction = versions.pickCompaction();
		if (compaction != null) {
			// 是minor compact
			if (compaction.isMinorCompaction()) {
				minorCompact(compaction);
			}
			// 是major compact
			else {
				majorCompact(compaction);
			}
		}
	}

	/**
	 * 在判断合适后，将memtable序列化成sstable
	 * 
	 * @throws IOException
	 */
	private void serializeMemTable() throws IOException {
		// compaction加锁
		compactionLock.lock();
		try {
			// immutable memtable不存在或为空，则说明不需要序列化
			if (immutableMemTable == null || immutableMemTable.isEmpty()) {
				return;
			}
			// 新建version和versionEdit
			VersionEdit edit = new VersionEdit();
			Version version = versions.getCurrent();
			// 将memTable中的数据持久化到sstable
			FileMetaData fileMetaData = memToSSTable(immutableMemTable);
			// 更新version及versionEdit信息
			if (fileMetaData != null && fileMetaData.getFileSize() > 0) {
				// 获取最值
//				ByteBuf minUserKey = fileMetaData.getSmallest().getKey();
//				ByteBuf maxUserKey = fileMetaData.getLargest().getKey();
				// immutable memtable序列化文件直接放入0层
				int level = 0;
//				if (version != null) {
//					level = version.pickLevelForMemTableOutput(minUserKey, maxUserKey);
//				}
				// 增加文件信息
				edit.addFile(level, fileMetaData);
			}
			// 设置日志编号
			edit.setLogNumber(log.getFileNumber());
			// 应用versionEdit
			versions.logAndApply(edit);
			// 清空immutableMemTable
			immutableMemTable = null;
			// 处理pending文件
			handlePendingFiles();
		} finally {
			try {
				// 唤醒等待compaction的线程
				backgroundCondition.signalAll();
			} finally {
				// 释放compaction锁
				compactionLock.unlock();
			}
		}
	}

	/**
	 * 执行minor compact
	 * 将文件移动到上面一层即可
	 * @param compaction
	 *            compact信息
	 */
	private void minorCompact(Compaction compaction) {
		Objects.requireNonNull(compaction);
		FileMetaData fileMetaData = compaction.getLevelInputs().get(0);
		compaction.getEdit().deleteFile(compaction.getLevel(), fileMetaData.getNumber());
		compaction.getEdit().addFile(compaction.getLevel() + 1, fileMetaData);
		versions.logAndApply(compaction.getEdit());
	}

	/**
	 * 执行major compact
	 * 
	 * @param compaction
	 * @throws IOException 
	 */
	private void majorCompact(Compaction compaction) throws IOException {
		Objects.requireNonNull(compaction);
		// 新建major compaction helper
		MajorCompactionHelper helper = new MajorCompactionHelper(compaction);
		// 获取低一层level信息
		List<FileMetaData> levelInputs = compaction.getLevelInputs();
		// 获取高一层level信息
		List<FileMetaData> levelUpInputs = compaction.getLevelUpInputs();
		// 建立优先队列的比较器
		Comparator<SeekingIterator<ByteBuf, ByteBuf>> comparator = new SeekingIteratorComparator(internalKeyComparator);
		// 新建优先队列，优先队列以迭代器的当前元素大小来排序
		int sorterSize = levelInputs.size() + levelUpInputs.size();
		PriorityQueue<SeekingIterator<ByteBuf, ByteBuf>> sorter = new PriorityQueue(sorterSize, comparator);
		// 将所有sstable的迭代器加入到优先队列中
		levelInputs.forEach(fileMetaData -> {
			// 根据file获取sstable
			SSTable sstable = null;
			try {
				sstable = tableCache.getTable(fileMetaData.getNumber());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Preconditions.checkNotNull(sstable);
			// 获取sstable中的迭代器并放入优先队列
			SeekingIterator<ByteBuf, ByteBuf> iter = sstable.iterator();
			sorter.add(iter);
		});
		// 同上
		levelUpInputs.forEach(fileMetaData -> {
			// 根据file获取sstable
			SSTable sstable = null;
			try {
				sstable = tableCache.getTable(fileMetaData.getNumber());
			} catch (Exception e) {
				e.printStackTrace();
			}
			Preconditions.checkNotNull(sstable);
			// 获取sstable中的迭代器并放入优先队列
			SeekingIterator<ByteBuf, ByteBuf> iter = sstable.iterator();
			sorter.add(iter);
		});
		
		// major compaction时释放锁
		compactionLock.unlock();
		try {
			// 记录上一个internalKey，用来判断key是否重复
			InternalKey lastKey = null;
			// 优先队列中至少需要两个迭代器
			while (sorter.size() > 0) {
				// memtable的序列化拥有最高优先级，因为需要保证用户能不断写入数据
				compactionLock.lock();
				try {
					serializeMemTable();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					compactionLock.unlock();
				}
				
				// 取出优先队列中的当前数据最小的迭代器
				SeekingIterator<ByteBuf, ByteBuf> iter = sorter.poll();
				// 取出迭代器当前位置数据，并移动到下一个位置
				Entry<ByteBuf, ByteBuf> entry = iter.next();
				InternalKey thisKey = InternalKey.decode(entry.getKey());
				// 如果数据重复，抛弃，否则加入到sstable中
				if (lastKey != null && lastKey.getKey().equals(thisKey.getKey())) {
					// 抛弃
				} else {
					// 不抛弃，处理数据
					// 新建builder
					if(!helper.hasBuilder()) {
						helper.newSSTable();
					}
					// 插入数据到builder
					helper.addToSSTable(entry.getKey(), entry.getValue());
					// builder已满，则终止插入，重置builder
					if(helper.isFull()) {
						helper.finishSSTable();
					}
					// 更新lastKey
					lastKey = thisKey;
					
				}
				// 如果迭代器到头了，则不放回，否则放回
				if (iter.hasNext()) {
					sorter.add(iter);
				}
			}
			// 终止builder
			helper.finishSSTable();
		} finally {
			compactionLock.lock();
		}
		//将新建文件信息加入到version中
		helper.installSSTable();
	}


	
	/**
	 * memTable数据持久化到sstable
	 * @param mem memTable
	 * @return 持久化得到文件的信息
	 * @throws IOException
	 */
	private FileMetaData memToSSTable(MemTable mem) throws IOException {
		// 必须在compactionLock锁住的临界区中
		if(!compactionLock.isHeldByCurrentThread()) {
			return null;
		}
		// 跳过空memtable
		if (mem.isEmpty()) {
			return null;
		}
		// 新建sst文件
		long fileNumber = versions.getNextFileNumber();
		File file = FileUtils.newSSTableFile(databaseDir, fileNumber);
		FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

		// 最值
		InternalKey smallest = null;
		InternalKey largest = null;

		// TODO
		SSTableBuilder tableBuilder = null;

		compactionLock.lock();
		try {
			// 遍历迭代器
			SeekingIterator<InternalKey, ByteBuf> iter = mem.iterator();
			while (iter.hasNext()) {
				Entry<InternalKey, ByteBuf> entry = iter.next();
				InternalKey key = entry.getKey();
				// 设置最值
				if (smallest == null) {
					smallest = key;
				}
				largest = key;
				// 将数据加入table中
				tableBuilder.add(entry);
			}
			// 完成table的构造
			tableBuilder.finish();
		} finally {
			compactionLock.unlock();
		}
		// 获取sstable文件信息
		FileMetaData fileMetaData = new FileMetaData(fileNumber, file.length(), smallest, largest);
		return fileMetaData;
	}
	
	/**
	 * 处理pending的file
	 */
	private void handlePendingFiles() {
		//TODO
	}
	
	/**
	 * major compaction过程中的辅助类
	 * @author bird
	 */
	private class MajorCompactionHelper{
		private final Compaction compaction;
		//compaction后输出的若干file
		private final List<FileMetaData> fileMetaDatas = new ArrayList<>();
		// 当前sstable
		private FileChannel channel;
		private SSTableBuilder builder;
		// 当前文件信息
		private long currentFileNumber;
		private long currentFileSize;
		private InternalKey currentSmallest;
		private InternalKey currentLargest;
		// 所有新建文件的总大小
		private long totalBytes;
		
		MajorCompactionHelper(Compaction compaction){
			this.compaction = compaction;
		}
		
		/**
		 * 检验是否存在builder
		 * @return
		 */
		boolean hasBuilder() {
			return builder == null;
		}
		/**
		 * 判断sstable是否达到容量限制
		 * @return
		 * @throws IOException 
		 */
		boolean isFull() throws IOException {
			// 比较当前sstable文件大小和限制大小
			return builder.getFileSize() >= compaction.getMaxOutputFileSize();
		}
		/**
		 * 新建sstable
		 * @throws IOException 
		 */
		void newSSTable() throws IOException {
			compactionLock.lock();
			try {
				// 初始化文件信息
				currentFileNumber = versions.getNextFileNumber();
				pendingFileNumbers.add(currentFileNumber);
				currentFileSize = 0;
				currentSmallest = null;
				currentLargest = null;
				// 新建文件加入pending
				pendingFileNumbers.add(currentFileNumber);
				// 初始化sstable文件和sstable builder
				File file = FileUtils.newSSTableFile(databaseDir, currentFileNumber);
				channel = new RandomAccessFile(file, "rw").getChannel();
				//TODO
				builder = null;
			} finally {
				compactionLock.unlock();
			}
		}
		/**
		 * 数据添加到sstable中
		 * @param key 键
		 * @param value 值
		 * @throws IOException 
 		 */
		void addToSSTable(ByteBuf key, ByteBuf value) throws IOException {
			// 更新最值
			if(currentSmallest == null) {
				currentSmallest = InternalKey.decode(key);
			}
			currentLargest =InternalKey.decode(key);
			// 插入数据到sstable
			builder.add(key, value);
		}
		/**
		 * 完成sstable
		 * @throws IOException 
		 */
		void finishSSTable() throws IOException {
			if(builder == null)
				return;
			// 结束build
			builder.finish();
			// 更新文件信息
			currentFileSize = builder.getFileSize();
			totalBytes += currentFileSize;
			// 整理文件信息
			FileMetaData currentFileMetaData = new FileMetaData(currentFileNumber,currentFileSize, currentSmallest, currentLargest);
			fileMetaDatas.add(currentFileMetaData);
			// 清空builder
			builder = null;
			// fileChannel刷到硬盘，关闭channel并清空
			channel.force(true);
			channel.close();
			channel = null;
		}
		/**
		 * sstable信息记录到version中
		 */
		void installSSTable() {
			VersionEdit edit = compaction.getEdit();
			// 将刚刚参与归并的sstable文件作为待删除文件，加入到compaction中
			compaction.addInputDeletions(compaction.getEdit());
			int level = compaction.getLevel();
			// 新建的文件应当加到更上一层中
			for(FileMetaData newFile : fileMetaDatas) {
				edit.addFile(level + 1, newFile);
				// 从pending列表中移除
				pendingFileNumbers.remove(newFile.getNumber());
			}
			//记录并且应用versionEdit
			versions.logAndApply(edit);
			// 删除失效文件
			handlePendingFiles();
		}
	}
}
