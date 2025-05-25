package MasterManagers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;

import MasterManagers.SocketManager.SocketThread;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TableManager {

    // 1. 表名到ip的映射关系
    private final Map<String, List<String>> tableToIP;
    // 记录每个表存储在哪个Region服务器上，支持同一个表存储在不同Region服务器
    // 例: {"table-1" -> ["127.0.0.1", "127.0.0.2"]}

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

    // 5. 轮转索引映射表（表名 -> 原子计数器）
    private final ConcurrentHashMap<String, AtomicInteger> tableIndices = new ConcurrentHashMap<>();

    /**
     * 获取表的下一个Region服务器（轮转方式）
     *
     * @param tableName 表名
     * @return 下一个Region服务器IP，若表不存在或无可用服务器则返回null
     */
    public String getNextRegionIP(String tableName) {
        // 获取表对应的服务器列表
        List<String> regionIPs = tableToIP.get(tableName);
        if (regionIPs == null || regionIPs.isEmpty()) {
            log.warn("表 {} 不存在或无可用Region服务器", tableName);
            return null;
        }

        // 获取或创建原子计数器
        AtomicInteger indexCounter = tableIndices.computeIfAbsent(tableName, k -> new AtomicInteger(0));

        synchronized (indexCounter) { // 同步块确保原子性操作
            int currentIndex = indexCounter.getAndIncrement();
            int size = regionIPs.size();
            currentIndex %= size; // 确保索引在有效范围内
            if (currentIndex < 0) { // 处理可能的负数（虽然getAndIncrement不会产生负数）
                currentIndex += size;
            }
            return regionIPs.get(currentIndex);
        }
    }

    /**
     * 重置指定表的轮转索引（用于测试或特殊场景）
     *
     * @param tableName 表名
     */
    public void resetTableIndex(String tableName) {
        tableIndices.remove(tableName);
    }

    public TableManager() throws IOException {
        // 初始化数据结构
        tableToIP = new HashMap<>();
        IPList = new ArrayList<>();
        aliveIPToTable = new HashMap<>();
        IPToSocketThread = new HashMap<>();
    }

    /**
     * 返回IPList中已经注册的Region服务器的数量
     *
     * @return 返回IPList中已经注册的Region服务器数量
     */
    public int getIPListSize() {
        return IPList.size();
    }

    /**
     * 返回现在所有活跃的Region服务器
     *
     * @return 返回现在所有活跃的Region服务器的List
     */
    public List<String> getAllAliveIPs() {
        return new ArrayList<>(aliveIPToTable.keySet());
    }

    /**
     * 添加新表到指定Region服务器，如果表已存在则添加新的Region服务器
     *
     * @param tableName 表名
     * @param regionIP Region服务器IP
     * @return true 表示成功添加; false 表示添加失败，指定Region服务器故障或其他原因，无法添加表
     */
    public boolean addTable(String tableName, String regionIP) {
        if (regionIP.equals("")) {
            return false;
        }
        // 如果Region服务器不在已知列表中，先添加
        if (!isExistServer(regionIP)) {
            addServer(regionIP);
        }

        // 如果Region服务器不在活跃列表中，说明其发生故障
        if (!isAliveServer(regionIP)) {
            return false;
        }

        // 更新表名到ip的映射关系
        if (!tableToIP.containsKey(tableName)) {
            tableToIP.put(tableName, new ArrayList<>());
        }
        if (!tableToIP.get(tableName).contains(regionIP)) {
            tableToIP.get(tableName).add(regionIP);
        }

        // 更新当前活跃服务器以及其存储的表名
        if (!aliveIPToTable.get(regionIP).contains(tableName)) {
            aliveIPToTable.get(regionIP).add(tableName);
        }

        return true;
    }

    /**
     * 添加多个新表到指定Region服务器，如果表已存在则添加新的Region服务器
     *
     * @param tableNames 表名列表
     * @param regionIP Region服务器IP
     * @return true 表示成功添加; false 表示添加失败，指定Region服务器故障或其他原因，无法添加表
     */
    public boolean addTables(List<String> tableNames, String regionIP) {
        // 如果Region服务器不在已知列表中，先添加
        if (!isExistServer(regionIP)) {
            addServer(regionIP);
        }

        // 如果Region服务器不在活跃列表中，说明其发生故障
        if (!isAliveServer(regionIP)) {
            return false;
        }

        for (String tableName : tableNames) {
            // 更新表名到ip的映射关系
            if (!tableToIP.containsKey(tableName)) {
                tableToIP.put(tableName, new ArrayList<>());
            }
            if (!tableToIP.get(tableName).contains(regionIP)) {
                tableToIP.get(tableName).add(regionIP);
            }
        }

        // 更新当前活跃服务器以及其存储的表名
        for (String tableName : tableNames) {
            if (!aliveIPToTable.get(regionIP).contains(tableName)) {
                aliveIPToTable.get(regionIP).add(tableName);
            }
        }

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

        // 检查表是否存在于指定的Region服务器
        if (!tableToIP.get(tableName).contains(regionIP)) {
            return false;
        }

        // 更新表名到ip的映射关系
        tableToIP.get(tableName).remove(regionIP);
        if (tableToIP.get(tableName).isEmpty()) {
            tableToIP.remove(tableName);
        }

        // 更新当前活跃服务器以及其存储的表名
        aliveIPToTable.get(regionIP).removeIf(tableName::equals);

        return true;
    }

    /**
     * 从所有服务器上删除指定表
     *
     * @param tableName 要删除的表名
     * @return true 表示删除成功；false 表示表不存在或删除失败
     */
    public boolean deleteTableFromAllServers(String tableName) {
        // 检查表是否存在
        if (!tableToIP.containsKey(tableName)) {
            return false;
        }

        // 获取存储该表的所有Region服务器
        List<String> regionIPs = new ArrayList<>(tableToIP.get(tableName));

        // 从每个服务器上删除该表
        for (String regionIP : regionIPs) {
            // 更新当前活跃服务器以及其存储的表名
            if (aliveIPToTable.containsKey(regionIP)) {
                aliveIPToTable.get(regionIP).removeIf(tableName::equals);
            }
        }

        // 完全移除表名到ip的映射关系
        tableToIP.remove(tableName);

        return true;
    }

    /**
     * 查找表所在的所有Region服务器。
     *
     * @param tableName 表名
     * @return Region服务器IP列表，如果表不存在则返回null
     */
    public List<String> getRegionIPs(String tableName) {
        return tableToIP.get(tableName);
    }

    /**
     * 查找表所在的任意一个Region服务器。
     *
     * @param tableName 表名
     * @return 任意一个Region服务器IP，如果表不存在则返回null
     */
    public String getOneRegionIP(String tableName) {
        List<String> ips = tableToIP.get(tableName);
        return (ips != null && !ips.isEmpty()) ? ips.get(0) : null;
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
     * 恢复故障服务器IP，将其重新加入活跃服务器列表
     *
     * @param regionIP 要恢复的Region服务器IP
     * @param tableNames 该服务器上存储的表名列表
     * @return true表示恢复成功，false表示服务器不存在或已经是活跃状态
     */
    public boolean recoverServer(String regionIP, List<String> tableNames) {
        // 检查服务器是否存在
        if (!isExistServer(regionIP)) {
            return false;
        }

        // 检查服务器是否已经是活跃状态
        if (isAliveServer(regionIP)) {
            return false;
        }

        // 将服务器重新加入活跃列表
        aliveIPToTable.put(regionIP, new ArrayList<>(tableNames));

        // 更新表名到IP的映射关系
        for (String tableName : tableNames) {
            if (!tableToIP.containsKey(tableName)) {
                tableToIP.put(tableName, new ArrayList<>());
            }
            if (!tableToIP.get(tableName).contains(regionIP)) {
                tableToIP.get(tableName).add(regionIP);
            }
        }

        return true;
    }

    /**
     * 检查该Region服务器是否已经注册
     *
     * @param regionIP Region服务器IP
     * @return true 存在该服务器; false 不存在该服务器
     */
    public boolean isExistServer(String regionIP) {
        log.info("查询节点是否已经注册: {}, {}", regionIP, IPList.contains(regionIP));
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
     * 带版本校验的安全索引重置方法
     *
     * @param tableName 表名
     * @param newActiveServer 新活跃服务器（用于日志追踪）
     */
    private void resetTableIndexWithValidation(String tableName, String newActiveServer) {
        // 1. 双重校验确保表存在性
        if (!tableToIP.containsKey(tableName)) {
            log.debug("表 {} 在转移后已不存在，跳过索引重置", tableName);
            return;
        }

        // 2. 获取当前服务器列表版本
        List<String> currentServers = tableToIP.get(tableName);
        int expectedVersion = currentServers.hashCode();

        // 3. 执行重置操作
        AtomicInteger indexCounter = tableIndices.get(tableName);
        if (indexCounter == null) {
            return;
        }

        synchronized (indexCounter) {
            // 4. 版本校验防止A-B-A问题
            if (currentServers.hashCode() != expectedVersion) {
                log.warn("表 {} 的服务器列表已变更，取消索引重置", tableName);
                return;
            }

            // 5. 执行安全重置
            indexCounter.set(0);
            log.debug("表 {} 的访问索引已重置（新活跃服务器: {}）", tableName, newActiveServer);
        }
    }

    /**
     * 增强版表转移方法（包含版本控制）
     *
     * @param sourceIP 源服务器
     * @param targetIP 目标服务器
     * @return 转移结果
     */
    public boolean transferTables(String sourceIP, String targetIP) {
        // 1. 参数校验
        if (sourceIP.equals(targetIP)) {
            log.warn("源服务器和目标服务器不能相同");
            return false;
        }

        // 2. 获取源服务器表列表（带版本快照）
        List<String> tableList = new ArrayList<>(getTableList(sourceIP));
        if (tableList.isEmpty()) {
            log.info("源服务器 {} 无表可转移", sourceIP);
            return true;
        }

        if (tableList.isEmpty()) {
            log.info("服务器 {} 无表需要转移，直接下线", sourceIP);
            aliveIPToTable.remove(sourceIP);
            return true;
        }

        // 3. 执行批量添加（带版本控制）
        Map<String, List<String>> originalTableMap = new HashMap<>(tableToIP);
        boolean addSuccess = addTables(tableList, targetIP);
        if (!addSuccess) {
            log.error("目标服务器 {} 添加表失败", targetIP);
            return false;
        }

        // 4. 版本校验确保数据一致性
        for (String tableName : tableList) {
            if (!originalTableMap.get(tableName).contains(sourceIP)
                    || !tableToIP.get(tableName).contains(targetIP)) {
                log.error("表 {} 转移过程中数据不一致，回滚操作", tableName);
                // 这里可以添加回滚逻辑（删除目标服务器上的表）
                return false;
            }
        }

        // 5. 清理源服务器数据
        aliveIPToTable.remove(sourceIP);
        for (String tableName : tableList) {
            if (tableToIP.get(tableName).remove(sourceIP) && tableToIP.get(tableName).isEmpty()) {
                tableToIP.remove(tableName);
            }
        }

        log.debug("表 {} 从 {} 成功转移到 {}", tableList, sourceIP, targetIP);

        // 6. 重置受影响表的轮转索引（带版本校验）
        for (String tableName : tableList) {
            resetTableIndexWithValidation(tableName, targetIP);
        }

        log.info("成功完成 {} 到 {} 的故障转移，{} 个表索引已重置",
                sourceIP, targetIP, tableList.size());

        return true;
    }

    /**
     * 测试时调用方法，用于初始化类
     */
    public void clearAll() {
        // 1. 清空表到IP的映射关系
        tableToIP.clear();

        // 2. 清空所有已知Region服务器列表
        IPList.clear();

        // 3. 清空活跃服务器及其存储的表
        aliveIPToTable.clear();

        // 4. 关闭并清空所有Socket连接
        for (SocketThread socketThread : IPToSocketThread.values()) {
            if (socketThread != null && !socketThread.isClosed()) {
                socketThread.close();
            }
        }
        IPToSocketThread.clear();
    }
}
