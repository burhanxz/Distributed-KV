# RPC-middleware
a  RPC middleware based on netty<br>
客户端结构图 structure of client(partial view):<br>
![image](https://github.com/burhanxz/RPC-middleware/blob/master/client%E7%BB%93%E6%9E%84)<br>

服务注册机制的时序图 service-register sequence diagram:<br>
![image](https://github.com/burhanxz/RPC-middleware/blob/master/%E6%9C%8D%E5%8A%A1%E6%B3%A8%E5%86%8C%E7%9A%84%E6%97%B6%E5%BA%8F%E5%9B%BE.png)<br>
- one can use this middleware like using java rmi;
- client pool and long-term connection;
- client heartbeat and reconnect machanism;
- protostuff serializaion;
- TCP stick package problem solved;
