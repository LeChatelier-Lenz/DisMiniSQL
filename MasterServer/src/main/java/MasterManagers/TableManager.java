package MasterManagers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    public TableManager() throws IOException {
        // 初始化数据结构
        tableToIP = new HashMap<>();
        IPList = new ArrayList<>();
        aliveIPToTable = new HashMap<>();
        IPToSocketThread = new HashMap<>();
    }

    /**
     * 添加新表到指定Region服务器。
     *
     * @param tableName 表名
     * @param regionIP Region服务器IP
     * @return true表示添加成功，false表示表已存在
     */
    public boolean addTable(String tableName, String regionIP) {
        // 检查表是否已存在
        if (tableToIP.containsKey(tableName)) {
            return false; // 表已存在，添加失败
        }

        // 如果Region服务器不在已知列表中，先添加
        if (!IPList.contains(regionIP)) {
            IPList.add(regionIP);
        }

        // 更新表名到ip的映射关系
        tableToIP.put(tableName, regionIP);

        // 更新当前活跃服务器以及其存储的表名
        aliveIPToTable.computeIfAbsent(regionIP, k -> new ArrayList<>()).add(tableName);

        return true;
    }

    /**
     * 在指定Region服务器删除表。
     *
     * @param tableName 表名
     * @param regionIP Region服务器IP
     * @return true表示删除成功，false表示表不存在或该Region服务器不包含该表
     */
    public boolean deleteTable(String tableName, String regionIP) {
        // 检查表是否存在
        if (!tableToIP.containsKey(tableName)) {
            return false;
        }

        // 检查表是否属于指定的Region服务器
        if (!regionIP.equals(tableToIP.get(tableName))) {
            return false;
        }

        // 更新表名到ip的映射关系
        tableToIP.remove(tableName);

        // 更新当前活跃服务器以及其存储的表名
        aliveIPToTable.get(regionIP).removeIf(tableName::equals);

        return true;
    }

    /**
     * 查找表所在Region服务器。
     *
     * @param tableName 表名
     * @return Region服务器IP
     */
    public String getRegionIP(String tableName) {
        return tableToIP.get(tableName);
    }

}
