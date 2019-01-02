package lsm.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import io.netty.buffer.ByteBuf;
import lsm.MemTable;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.Version;
import lsm.VersionEdit;
import lsm.VersionSet;
import lsm.base.Compaction;
import lsm.base.FileMetaData;
import lsm.base.FileUtils;
import lsm.base.InternalKey;
import lsm.base.Options;
import lsm.base.SeekingIteratorComparator;

public class Engine {
	/**
	 * 引擎基本配置
	 */
	private final Options options;
	
	/**
	 * 引擎工作目录
	 */
	private final File databaseDir;
	private final VersionSet versions;
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

	
	public Engine(Options options, File databaseDir) {
		//TODO
		this.options = options;
		this.databaseDir = databaseDir;
		this.versions = null;
		this.internalKeyComparator = null;
		this.compactionExecutor = null;
	}
	
	
	/**
	 * 尝试触发compact
	 */
	private void maybeCompaction() {
		// 不应compact的判断
		boolean shouldNotStartCompact = backgroundCompaction != null //上次后台合并未结束
				|| shuttingDown.get() //系统关闭
				|| (immutableMemTable == null && !versions.needsCompaction());// 在内存没有数据需要序列化的情况下，versionSet中未显示需要compact 
		if(!shouldNotStartCompact) {
			// 启动compact线程
			// 启动后台compaction线程
			backgroundCompaction = compactionExecutor.submit(new Callable<Void>() {
				@Override
				public Void call() throws Exception {
					//TODO 还需考虑线程安全等情况
					try {
						backgroundCompaction();
					} 
					catch(IOException e) {
						e.printStackTrace();
					}
					finally {
						// 再次触发compact
						maybeCompaction();
					}
					
					return null;
				}
			});
		}
	}
	
	private void backgroundCompaction() throws IOException {
		// 尝试序列化memtable，优先级最高
		serializeMemTable();
		// 获取compaction信息
		Compaction compaction = versions.pickCompaction();
		if(compaction != null) {
			// 是minor compact
			if(compaction.isMinorCompaction()) {
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
	 * @throws IOException 
	 */
	private void serializeMemTable() throws IOException {
		//TODO
		// immutable memtable不存在或为空，则说明不需要序列化
		if(immutableMemTable == null || immutableMemTable.isEmpty()) {
			return;
		}
		SeekingIterator<InternalKey, ByteBuf> iter = immutableMemTable.iterator();
		VersionEdit edit = new VersionEditImpl();
		Version base = versions.getCurrent();
		// sst文件
		long fileNumber = versions.getNextFileNumber();
		File file = FileUtils.newSSTableFile(databaseDir, fileNumber);
		FileChannel channel = new RandomAccessFile(file, "rw").getChannel();

		// 最值
		InternalKey smallest = null;
		InternalKey largest = null;
		
		// TODO
		SSTableBuilder tableBuilder = null;
		
		// 遍历迭代器
		while(iter.hasNext()) {
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
		// 获取sstable文件信息
		FileMetaData fileMetaData = new FileMetaData(fileNumber, file.length(), smallest, largest);
		// 更新version及versionEdit信息
	}
	
	/**
	 * 执行minor compact
	 * @param compaction compact信息
	 */
	private void minorCompact(Compaction compaction) {
		FileMetaData fileMetaData = compaction.getLevelInputs().get(0);
		compaction.getEdit().deleteFile(compaction.getLevel(), fileMetaData.getNumber());
		compaction.getEdit().addFile(compaction.getLevel() + 1, fileMetaData);
		versions.logAndApply(compaction.getEdit());
	}
	
	/**
	 * 执行major compact
	 * @param compaction
	 */
	private void majorCompact(Compaction compaction) {
		// TODO
		long lastSeq = versions.getLastSequence();
		// 获取低一层level信息
		List<FileMetaData> levelInputs = compaction.getLevelInputs();
		// 获取高一层level信息
		List<FileMetaData> levelUpInputs = compaction.getLevelUpInputs();
		// 建立优先队列的比较器
		Comparator<SeekingIterator<ByteBuf, ByteBuf>> comparator = 
				new SeekingIteratorComparator(internalKeyComparator);
		// 新建优先队列，优先队列以迭代器的当前元素大小来排序
		int sorterSize = levelInputs.size() + levelUpInputs.size();
		PriorityQueue<SeekingIterator<ByteBuf, ByteBuf>> sorter = new PriorityQueue(sorterSize, comparator);
		// 将所有sstable的迭代器加入到优先队列中
		levelInputs.forEach(fileMetaData -> {
			// 根据file获取sstable
			SSTable sstable = new SSTableImpl(fileMetaData);
			// 获取sstable中的迭代器并放入优先队列
			SeekingIterator<ByteBuf, ByteBuf> iter = sstable.iterator();
			sorter.add(iter);
		});
		// 同上
		levelUpInputs.forEach(fileMetaData -> {
			// 根据file获取sstable
			SSTable sstable = new SSTableImpl(fileMetaData);
			// 获取sstable中的迭代器并放入优先队列
			SeekingIterator<ByteBuf, ByteBuf> iter = sstable.iterator();
			sorter.add(iter);
		});
		
		// 记录上一个internalKey
		InternalKey lastKey = null;
		// 优先队列中至少需要两个迭代器
		while(sorter.size() > 1) {
			// 取出优先队列中的当前数据最小的迭代器
			SeekingIterator<ByteBuf, ByteBuf> iter = sorter.poll();
			// 取出迭代器当前位置数据，并移动到下一个位置
			Entry<ByteBuf, ByteBuf> entry = iter.next();
			InternalKey thisKey = new InternalKey(entry.getKey());
			// 如果数据重复，抛弃，否则加入到sstable中
			if(lastKey != null && lastKey.getKey().equals(thisKey.getKey())) {
				// 抛弃
			}
			else {
				// 更新lastKey
				lastKey = thisKey;
				// 数据加入到sstable中
				addToSSTable(entry);
			}
			// 如果迭代器到头了，则不放回，否则放回
			if(iter.hasNext()) {
				sorter.add(iter);
			}
		}

		
	}
	
	/**
	 * 加入数据到sstable中，当sstable大小达到限制则新建sstable
	 * @param entry
	 */
	public void addToSSTable(Entry<ByteBuf, ByteBuf> entry) {
		//TODO
	}
}