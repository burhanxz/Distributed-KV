package com.xuzhong.rpc.test.clientFactory.pojo;

public class RemotingUrl {
    /**
     * 域名
     **/
    private String domain;

    /**
     * 端口
     **/
    private int port;

    /**
     * 超时时间,默认3s
     **/
    private int connectionTimeout = 3000;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((domain == null) ? 0 : domain.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return this.domain.equalsIgnoreCase((String) obj);
    }

}