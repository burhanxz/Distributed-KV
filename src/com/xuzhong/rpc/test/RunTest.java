package com.xuzhong.rpc.test;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Scanner;

import org.junit.Test;

import com.xuzhong.rpc.facet.Log;
import com.xuzhong.rpc.service.ComputeService;
import com.xuzhong.rpc.service.NameService;
import com.xuzhong.rpc.zookeeper.IRegistry;
import com.xuzhong.rpc.zookeeper.IRegistryFactory;

public class RunTest {

	class myThread extends Thread {
		private int num;

		public myThread(int num) {
			this.num = num;
		}

		@Override
		public void run() {
			Log.logger.info("run");
			IRegistry iRegistry;
			try {
				iRegistry = IRegistryFactory.getInstance().getZkRegistry(new InetSocketAddress("127.0.0.1", 3000));
				
				//测试代码中，应当在zookeeper后台注册好127.0.0.1:8080
				NameService nameServiceStub = iRegistry.lookup(NameService.class);

				Log.logger.info("@" + nameServiceStub.getName());

				ComputeService computeServiceStub = iRegistry.lookup(ComputeService.class);

				Log.logger.info("#" + computeServiceStub.getName(new int[] { this.num }));

				Log.logger.info("#" + computeServiceStub.compute(new int[] { num, num, num }));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

//	@Test
	public static void main(String[] args) throws Exception {
//		Scanner scan = new Scanner(System.in);
//		Random random = new Random();
//		while (true) {
//			for (int i = 0; i != 1; i++) {
//				Log.logger.info("============================thread " + i + " ===============================");
//				new myThread(i).start();
//			}
//			//block
//			String s = scan.next();
//			
//		}
		new RunTest().new myThread(1).start();
	}
}
