package com.xuzhong.rpc.test;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Scanner;

import org.junit.Test;

import com.xuzhong.rpc.facet.Log;
import com.xuzhong.rpc.iRegistry.IRegistry;
import com.xuzhong.rpc.iRegistry.IRegistryFactory;
import com.xuzhong.rpc.service.ComputeService;
import com.xuzhong.rpc.service.NameService;

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
				iRegistry = IRegistryFactory.getInstance().getIRegistry(new InetSocketAddress("127.0.0.1", 5000));

				NameService nameService = iRegistry.getService(NameService.class);

				Log.logger.info("@" + nameService.getName());

				ComputeService computeService = iRegistry.getService(ComputeService.class);

				Log.logger.info("#" + computeService.getName(new int[] { this.num }));

				Log.logger.info("#" + computeService.compute(new int[] { num, num, num }));

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	@Test
	public void test() throws Exception {
		Scanner scan = new Scanner(System.in);
		Random random = new Random();
		while (true) {
			for (int i = 0; i != 1; i++) {
				Log.logger.info("============================thread " + i + " ===============================");
				new myThread(i).start();
			}
			//block
			String s = scan.next();
			
		}

	}
}
