# MicroServices-Framework

- one can use this middleware like using java rmi;
- client pool and long-term connection; 
- client heartbeat and reconnect machanism，TCP stick package problem solved;
- protostuff serializaion;
- service register machanism based on zookeeper;


客户端结构图 structure of client:<br>
![image](https://github.com/burhanxz/Microservices-Framework/blob/master/doc/client%E7%BB%93%E6%9E%84.png)<br>

服务注册机制的时序图 service-register sequence diagram:<br>
![image](https://github.com/burhanxz/Microservices-Framework/blob/master/doc/%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E7%9A%84%E6%97%B6%E5%BA%8F%E5%9B%BE.png)<br>
- 此中间件可以像使用Java rmi一样使用;
- 客户端连接池; 
- 完善的心跳包、重连、粘包解决机制;
- 高效序列化与传输;
- 基于zookeeper的服务注册机制，实时获取最新服务列表;


