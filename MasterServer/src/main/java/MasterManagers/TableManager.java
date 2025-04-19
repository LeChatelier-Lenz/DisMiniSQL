package MasterManagers;

import java.util.List;
import java.util.Map;

public class TableManager {

    // 1. 表名到ip的映射关系
    private Map<String, String> tableToIP;
    // 记录每个表存储在哪个Region服务器上
    // 例: {"table-1" -> "127.0.0.1"}

    // 2. 所有已知Region服务器的List
    private List<String> IPList;
    // 记录所有注册的Region服务器IP地址
    // 例: {"127.0.0.1", "192.168.1.1"}
}
