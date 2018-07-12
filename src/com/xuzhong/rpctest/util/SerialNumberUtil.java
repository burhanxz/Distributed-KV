package com.xuzhong.rpctest.util;

import java.util.Random;

public class SerialNumberUtil {
	private final static int ID_SIZE = 16;
	
	public static long makeSerialNumber() {
		
		StringBuffer stringBuffer = new StringBuffer();
		
		Random random = new Random();
		
		for(int i = 0; i != ID_SIZE; i++) {
			stringBuffer.append(String.valueOf(random.nextInt(9)));
		}
		
		return Long.valueOf(stringBuffer.toString());
	}
}
