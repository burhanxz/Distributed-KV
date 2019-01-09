package lsm.internal;

import java.util.NoSuchElementException;
import java.util.Map.Entry;

import io.netty.buffer.ByteBuf;
import lsm.SeekingIterator;

public abstract class AbstractSeekingIterator implements SeekingIterator<ByteBuf, ByteBuf>{
	private Entry<ByteBuf, ByteBuf> next;
	@Override
	public boolean hasNext() {
		// 如果next为空，尝试获取下一个元素
        if (next == null) {
        	next = getNextElement();
        }
        // 检验下一个元素是否为空
        return next != null;
	}

	@Override
	public Entry<ByteBuf, ByteBuf> next() {
		// 如果next元素为空，尝试获取下一个元素
        if (next == null) {
        	next = getNextElement();
        	// 如果元素依然为空，抛出异常
            if (next == null) {
            	 throw new NoSuchElementException();
            }
        }
        // next置空，返回结果
        Entry<ByteBuf, ByteBuf> result = next;
        next = null;
        return result;
	}

	@Override
	public Entry<ByteBuf, ByteBuf> peek() {
		// 相比于next()，不置空元素
        if (next == null) {
        	next = getNextElement();
            if (next == null) {
                throw new NoSuchElementException();
            }
        }
        return next;
	}

	@Override
	public void seek(ByteBuf key) {
        next = null;
        seekInternal(key);
	}

	@Override
	public void seekToFirst() {
		next = null;
        seekToFirstInternal();
	}
	
    protected abstract void seekToFirstInternal();
    
    protected abstract void seekInternal(ByteBuf targetKey);
    
    protected abstract Entry<ByteBuf, ByteBuf> getNextElement();
}
