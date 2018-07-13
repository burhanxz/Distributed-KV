package com.xuzhong.rpc.service;

public class ComputeServiceImpl implements ComputeService {

	@Override
	public int compute(int... args) {
		int result = 0;
		for(int i = 0; i != args.length; i++) {
			int tmp = Integer.valueOf(args[i]);
			result += tmp;
		}
		return result;
	}

	@Override
	public String getName(int... args) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i!= args.length;i++) {
			sb.append(String.valueOf(args[i]));
		}
		return sb.toString();
	}
	
}
