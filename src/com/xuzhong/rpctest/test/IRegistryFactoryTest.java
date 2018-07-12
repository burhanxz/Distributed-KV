package com.xuzhong.rpctest.test;

import java.net.InetSocketAddress;

import org.junit.Test;

import com.xuzhong.rpctest.iRegistry.IRegistry;
import com.xuzhong.rpctest.iRegistry.IRegistryFactory;
import com.xuzhong.rpctest.service.ComputeService;
import com.xuzhong.rpctest.service.NameService;

public class IRegistryFactoryTest {

	class myThread extends Thread {
		private int num;
		
		public myThread(int num) {this.num = num;}
		
		@Override
		public void run() {
			System.out.println("run");
			IRegistry iRegistry;
			try {
				iRegistry = IRegistryFactory.getInstance().getIRegistry(new InetSocketAddress("127.0.0.1", 8080));

				NameService nameService = iRegistry.getService(NameService.class);

				System.out.println("@" + nameService.getName());

				ComputeService computeService = iRegistry.getService(ComputeService.class);

				System.out.println("#" + computeService.getName(new int[] { this.num }));

				System.out.println("#" + computeService.compute(new int[] { num, num, num }));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Test
	public void test() throws Exception {
		for (int i = 0; i != 10; i++) {
			System.out.println("============================thread "+ i +" ===============================");
			new myThread(i).start();
		}
		
		Thread.sleep(100000);
		
	}
}
