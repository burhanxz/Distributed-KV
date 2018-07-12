package com.xuzhong.rpctest.test.clientFactory;

import java.util.concurrent.TimeUnit;

import com.xuzhong.rpctest.test.clientFactory.pojo.RemotingUrl;

import io.netty.channel.Channel;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

public class NettyClient implements Client {
    /**
     * 共享定时器
     **/
    public static final Timer timer = new HashedWheelTimer();
    public static final int DEFAULT_HEARTBEAT_PERIOD = 3000;
    /*the channel*/
    private Channel ch;
    /*server url*/
    private RemotingUrl url;

    public NettyClient(RemotingUrl url, Channel ch) {
        this.ch = ch;
        this.url = url;
    }

    @Override
    public void startHeartBeat() throws Exception {
        timer.newTimeout(new TimerTask() {
            @Override
            public void run(Timeout timeout) throws Exception {
                try {
                    // 发送心跳请求
                } catch (Throwable t) {
                } finally {
                    timer.newTimeout(this, DEFAULT_HEARTBEAT_PERIOD, TimeUnit.SECONDS);
                }
            }
        }, DEFAULT_HEARTBEAT_PERIOD, TimeUnit.SECONDS);
    }

    @Override
    public String getDomain() {
        return url.getDomain();
    }

    @Override
    public boolean isConnected() {
        return ch.isActive();
    }

    @Override
    public void sendRequest(Object msg) throws Exception {
        ch.writeAndFlush(msg);
    }

}
