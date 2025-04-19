package MasterManagers;

import java.util.List;
import java.util.Map;

import MasterManagers.Socket.SocketThread;

public class TableManager {

    // 1. 表名到ip的映射关系
    private Map<String, String> tableToIP;
    // 记录每个表存储在哪个Region服务器上
    // 例: {"table-1" -> "127.0.0.1"}

    // 2. 所有已知Region服务器的List
    private List<String> IPList;
    // 记录所有注册的Region服务器IP地址
    // 例: ["127.0.0.1", "192.168.1.1"]

    // 3. 当前活跃服务器以及其存储的表名
    private Map<String, List<String>> aliveIPToTable;
    // 记录每个活跃的Region服务器存储了哪些表 
    // 例: {"192.168.1.1" -> ["table-1", "table-2"]}

    // 4. IP地址到Socket连接的映射关系
    private Map<String, SocketThread> IPToSocketThread;
    // 维护每个Region服务器的Socket连接
    // 例: {"192.168.1.1" -> SocketThread1}
}
