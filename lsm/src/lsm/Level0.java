package lsm;

import io.netty.buffer.ByteBuf;
import lsm.base.InternalKey;

public interface Level0 extends SeekingIterable<InternalKey, ByteBuf>{

}
