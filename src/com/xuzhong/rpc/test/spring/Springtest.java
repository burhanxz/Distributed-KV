package com.xuzhong.rpc.test.spring;

import com.xuzhong.rpc.service.NameService;
import com.xuzhong.rpc.util.ContextUtil;

import io.netty.bootstrap.Bootstrap;

public class Springtest {
	public static void main(String[] args) {
		
		System.out.println(ContextUtil.getBean(Bootstrap.class).toString());
	}
}
