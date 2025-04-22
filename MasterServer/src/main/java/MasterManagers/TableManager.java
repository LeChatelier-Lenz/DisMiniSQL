package MasterManagers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import MasterManagers.Socket.SocketThread;

public class TableManager {

    // 1. 表名到ip的映射关系
    private final Map<String, String> tableToIP;
    // 记录每个表存储在哪个Region服务器上
    // 例: {"table-1" -> "127.0.0.1"}

    // 2. 所有已知Region服务器的List
    private final List<String> IPList;
    // 记录所有注册的Region服务器IP地址
    // 例: ["127.0.0.1", "192.168.1.1"]

    // 3. 当前活跃服务器以及其存储的表名
    private final Map<String, List<String>> aliveIPToTable;
    // 记录每个活跃的Region服务器存储了哪些表 
    // 例: {"192.168.1.1" -> ["table-1", "table-2"]}

    // 4. IP地址到Socket连接的映射关系
    private final Map<String, SocketThread> IPToSocketThread;
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
     * 添加新表到指定Region服务器，如果表已存在则覆盖
     *
     * @param tableName 表名
     * @param regionIP Region服务器IP
     * @return true 表示成功添加; false 表示添加失败，指定Region服务器故障或其他原因，无法添加表
     */
    public boolean addTable(String tableName, String regionIP) {

        // 如果Region服务器不在已知列表中，先添加
        if (!isExistServer(regionIP)) {
            addServer(regionIP);
        }

        // 如果Region服务器不在活跃列表中，说明其发生故障
        if (isAliveServer(regionIP)) {
            return false;
        }

        // 更新表名到ip的映射关系
        tableToIP.put(tableName, regionIP);

        // 更新当前活跃服务器以及其存储的表名
        aliveIPToTable.get(regionIP).add(tableName);

        return true;
    }

    /**
     * 添加多个新表到指定Region服务器，如果表已存在则覆盖
     *
     * @param tableName 表名
     * @param regionIP Region服务器IP
     * @return true 表示成功添加; false 表示添加失败，指定Region服务器故障或其他原因，无法添加表
     */
    public boolean addTables(List<String> tableNames, String regionIP) {

        // 如果Region服务器不在已知列表中，先添加
        if (!isExistServer(regionIP)) {
            addServer(regionIP);
        }

        // 如果Region服务器不在活跃列表中，说明其发生故障
        if (isAliveServer(regionIP)) {
            return false;
        }

        for (String tableName : tableNames) {
            // 更新表名到ip的映射关系
            tableToIP.put(tableName, regionIP);
        }

        // 更新当前活跃服务器以及其存储的表名
        aliveIPToTable.get(regionIP).addAll(tableNames);

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

    /**
     * 注册新的Region服务器
     *
     * @param regionIP Region服务器IP
     * @return true 注册成功; false 注册失败
     */
    public boolean addServer(String regionIP) {
        // 如果已经存在该服务器，就不注册
        if (isExistServer(regionIP)) {
            return false;
        }

        // 不存在该服务器IP，将服务器加入列表
        IPList.add(regionIP);

        // 创建活跃服务器到表的映射
        List<String> temp = new ArrayList<>();
        aliveIPToTable.put(regionIP, temp);

        return true;
    }

    /**
     * 检查该Region服务器是否已经注册
     *
     * @param regionIP Region服务器IP
     * @return true 存在该服务器; false 不存在该服务器
     */
    public boolean isExistServer(String regionIP) {
        return IPList.contains(regionIP);
    }

    /**
     * 检查该Region服务器是否活跃
     *
     * @param regionIP Region服务器IP
     * @return true 存在该服务器; false 不存在该服务器
     */
    public boolean isAliveServer(String regionIP) {
        return aliveIPToTable.containsKey(regionIP);
    }

    /**
     * 获取某Region服务器上所有表
     *
     * @param regionIP Region服务器IP
     * @return 如果该Region服务器活跃，那么返回该服务器上的表列表; 如果该服务器不在活跃列表中，那么返回null
     */
    public List<String> getTableList(String regionIP) {
        return aliveIPToTable.get(regionIP);
    }

    /**
     * 获取当前负载最小的服务器
     *
     * @param regionIPs 可填入服务器IP，寻找负载最小服务器时，会忽略它们
     * @return 负载最小(除指定服务器)的服务器IP
     */
    public String getBestServer(String... regionIPs) {
        Integer min = Integer.MAX_VALUE;
        String bestServerIP = "";

        // 遍历除了指定IP以外的服务器，找到表数量最少的
        for (Map.Entry<String, List<String>> e : aliveIPToTable.entrySet()) {
            // 跳过指定IP的服务器
            boolean isExist = false;
            for (String regionIP : regionIPs) {
                if (e.getKey().equals(regionIP)) {
                    isExist = true;
                    break;
                }
            }
            if (isExist) {
                continue;
            }

            // 比较找到负载最小的服务器
            if (e.getValue().size() < min) {
                min = e.getValue().size();
                bestServerIP = e.getKey();
            }
        }
        return bestServerIP;
    }

    /**
     * 添加Region服务器的Socket连接
     *
     * @param regionIP Region服务器IP
     * @param socketThread Socket连接进程
     */
    public void addSocketThread(String regionIP, SocketThread socketThread) {
        IPToSocketThread.put(regionIP, socketThread);
    }

    /**
     * 获取服务器的Socket连接
     *
     * @param regionIP Region服务器IP
     * @return Socket连接进程
     */
    public SocketThread getSocketThread(String regionIP) {
        return IPToSocketThread.get(regionIP);
    }

    /**
     * 将一个Region服务器上的表转移到另一个Region服务器
     *
     * @param sourceIP 需要转移的源服务器，通常为故障服务器
     * @param targetIP 转移的目标服务器
     * @return true 表示转移成功; false 表示转移失败，或者源服务器和目标服务器相同
     */
    public boolean transferTables(String sourceIP, String targetIP) {
        // 源服务器不能和目标服务器相同
        if (sourceIP.equals(targetIP)) {
            return false;
        }

        // 转移源服务器上的表
        List<String> tableList = getTableList(sourceIP);
        if (tableList == null) {
            return true;
        }
        addTables(tableList, targetIP);

        // 在活跃服务器中删除故障服务器
        aliveIPToTable.remove(sourceIP);

        return true;
    }
}
