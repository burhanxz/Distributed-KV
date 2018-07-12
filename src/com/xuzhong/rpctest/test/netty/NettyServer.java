package com.xuzhong.rpctest.test.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;

public class NettyServer {  
    //ip 地址  
    private static   String IP = "127.0.0.1";  
    //默认端口  
    private  static  int PORT = 5656;  
      
    private static final int BIZGROUPSIZE = Runtime.getRuntime().availableProcessors()*2;  
      
    private static final int BIZTHREADSIZE = 100;  
    private static final EventLoopGroup bossGroup = new NioEventLoopGroup(BIZGROUPSIZE);  
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(BIZTHREADSIZE);  
    public static void init() throws Exception {  
          
        ServerBootstrap bootstrap = new ServerBootstrap();  
        bootstrap.group(bossGroup, workerGroup);  
        bootstrap.channel(NioServerSocketChannel.class);  
        bootstrap.childHandler(new ChannelInitializer<Channel>() {  
  
            @Override  
            protected void initChannel(Channel ch) throws Exception {  
                // TODO Auto-generated method stub  
                ChannelPipeline pipeline = ch.pipeline();  
                 pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));  
                    pipeline.addLast(new LengthFieldPrepender(4));  
                    pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));  
                    pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));  
                    pipeline.addLast(new ServerHandler());  
                    pipeline.addLast(new ServerOutHandler1());  
                    pipeline.addLast(new ServerOutHandler2()); 
                    
            }  
              
        });  
        IP =Configuration.getProperty("nioServerIp");  
        PORT=Integer.parseInt(Configuration.getProperty("nioServerPort"));  
        System.out.println("【TCP服务器IP】"+IP+"【TCP服务器PORT】"+PORT);  
          ChannelFuture f = bootstrap.bind(IP, PORT).sync();  
            f.channel().closeFuture().sync();  
            System.out.println("TCP服务器已启动");  
        }  
  
      
      
        protected static void shutdown() {  
            workerGroup.shutdownGracefully();  
            bossGroup.shutdownGracefully();  
        }  
  
        public static void main(String[] args) throws Exception {  
            System.out.println("初始化配置文件...");  
            Configuration.init();     
            System.out.println("开始启动TCP服务器...");  
            NettyServer.init();  
//         HelloServer.shutdown();  
        }  
}  


