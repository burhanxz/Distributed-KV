package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

public interface Level extends SeekingIterable<InternalKey, ByteBuf>{
	
}
