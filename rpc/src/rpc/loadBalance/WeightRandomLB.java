package rpc.loadBalance;

import java.util.List;

import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.URL;

public class WeightRandomLB implements LoadBalance {

	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		// TODO Auto-generated method stub
		return null;
	}

}
