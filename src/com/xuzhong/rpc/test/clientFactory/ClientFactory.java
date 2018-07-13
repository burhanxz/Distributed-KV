package com.xuzhong.rpc.test.clientFactory;

import java.util.List;

import com.xuzhong.rpc.test.clientFactory.pojo.RemotingUrl;

public interface ClientFactory {
    // ------------ 查询和新增 ------------
    Client get(final RemotingUrl url) throws Exception;

    // ------------ 查询 ------------
    List<Client> getAllClients() throws Exception;

    // ------------ 删除 ------------
    void remove(final RemotingUrl url) throws Exception;
}
