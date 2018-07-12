package com.xuzhong.rpctest.test.spring;

import com.xuzhong.rpctest.service.NameService;
import com.xuzhong.rpctest.util.ContextUtil;

import io.netty.bootstrap.Bootstrap;

public class Springtest {
	public static void main(String[] args) {
		
		System.out.println(ContextUtil.getBean(Bootstrap.class).toString());
	}
}
