package rpc.loadBalance;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import rpc.Invocation;
import rpc.Invoker;
import rpc.LoadBalance;
import rpc.URL;
import rpc.model.ProviderConstants;

/**
 * 利用加权随机方式获取节点
 * @author bird
 *
 */
public class WeightRandomLB implements LoadBalance {
	private static Random random = new Random();
	@Override
	public <T> Invoker<T> select(List<Invoker<T>> invokers, URL url, Invocation invocation) {
		// 利用Java Stream获取权重集合
		List<Integer> weights = invokers.stream()
				.map(invoker -> {
					int weight;
					String weightStr = invoker.getUrl().getParameters().get(ProviderConstants.WEIGHT);
					weight = weightStr == null ? 0 : Integer.parseInt(weightStr);
					return weight;
				})
				.collect(Collectors.toList());
		// 计算所有权重的总和
		int sum = weights.stream().reduce(Integer::sum).get();
		// 得到权重总和范围内的一个随机值
		int rand = random.nextInt(sum);
		// 计算随机值落在的位置
		int n;
		for(n = 0; n < weights.size(); n++) {
			int weight = 0;
			if((weight = weights.get(n)) > rand) {
				break;
			}
			rand -= weight;
		}
		// 返回对应位置的invoker
		return invokers.get(n);
	}

}
