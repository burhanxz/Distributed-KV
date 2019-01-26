package lsm.internal;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import io.netty.buffer.ByteBuf;
import lsm.SSTable;
import lsm.SeekingIterator;
import lsm.TableCache;
import lsm.base.InternalKey;
import lsm.base.LookupKey;
import lsm.base.LookupResult;

public class TableCacheImpl implements TableCache{
	private final File databaseDir;
	
	private final LoadingCache<Long, SSTable> cache;
	
	public TableCacheImpl(File databaseDir, int cacheSize) {
		this.databaseDir = databaseDir;
		cache = CacheBuilder.newBuilder().maximumSize(cacheSize)
				// 添加删除监听器
				.removalListener(new RemovalListener<Long, SSTable>() {

					@Override
					public void onRemoval(RemovalNotification<Long, SSTable> notification) {
						// 获取table并且关闭
						SSTable table = notification.getValue();
						try {
							table.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				})
				// 添加载入器
				.build(new CacheLoader<Long, SSTable>(){
					
					@Override
					public SSTable load(Long fileNumber) throws Exception {
						return new SSTableImpl(databaseDir, fileNumber);
					}
					
				});
	}
	
	@Override
	public SSTable getTable(long fileNumber) throws Exception {
		cache.get(fileNumber);
		return null;
	}

	@Override
	public LookupResult get(LookupKey key, long fileNumber, long fileSize) throws Exception {
		// 根据fileNumber获取sstable
		SSTable sstable = cache.get(fileNumber);
		// 获取sstable迭代器
		SeekingIterator<ByteBuf, ByteBuf> iter = sstable.iterator();
		InternalKey userKey = key.getInternalKey();
		ByteBuf keyBuffer = userKey.getUserKey();
		// 移动到key位置
		iter.seek(keyBuffer);
		// 如果key存在
		if(iter.hasNext()) {
			// 获取key
			Entry<ByteBuf, ByteBuf> entry = iter.next();
			InternalKey internalKey = InternalKey.decode(entry.getKey());
			// 如果key确实相等
			if(internalKey.getUserKey().equals(userKey.getUserKey())) {
				// 判断key的类型,并生成相应的返回结果
				if(internalKey.getType() == InternalKey.InternalKeyType.ADD) {
					return new LookupResult(key, null, InternalKey.InternalKeyType.DELETE);
				}
				else if(internalKey.getType() == InternalKey.InternalKeyType.DELETE) {
					return new LookupResult(key, entry.getValue(), InternalKey.InternalKeyType.ADD);
				}
			}
		}
		return null;
	}

	@Override
	public void evict(long number) {
		cache.invalidate(number);
	}
}
