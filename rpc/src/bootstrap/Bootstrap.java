package bootstrap;

/**
 * 微服务框架的启动类
 * @author bird
 *
 */
public class Bootstrap {

	public static void main(String[] args) {
		//连接ZooKeeper
		ZooKeeperConnection.getInstance().init();

	}

}
