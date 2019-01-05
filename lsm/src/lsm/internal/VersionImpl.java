package lsm.internal;

import io.netty.buffer.ByteBuf;
import lsm.Version;

public class VersionImpl implements Version{

	@Override
	public int pickLevelForMemTableOutput(ByteBuf smallest, ByteBuf largest) {
		// TODO Auto-generated method stub
		return 0;
	}

}
