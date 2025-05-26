package config;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TableLocationCache {
    private final ConcurrentHashMap<String, Set<String>> tableToIPs = new ConcurrentHashMap<>();

    // 缓存：替换表对应的所有 IP（用于从 master 返回一组 IP 的情况）
    public void replace(String table, List<String> ipList) {
        tableToIPs.put(table.toLowerCase(), new HashSet<>(ipList));
    }

    // 添加单个 IP 到表对应的 IP 集合
    public void add(String table, String ip) {
        tableToIPs.computeIfAbsent(table.toLowerCase(), k -> new HashSet<>()).add(ip);
    }

    // 原 cache 方法（用于设置单一 IP）
    public void cache(String table, String ip) {
        replace(table, Collections.singletonList(ip));
    }

    // 获取某张表的所有 IP 列表
    public List<String> getIPList(String table) {
        Set<String> set = tableToIPs.get(table.toLowerCase());
        if (set == null) return new ArrayList<>();
        return new ArrayList<>(set);
    }

    // 获取一个 IP（用于兼容旧逻辑）
    public String getIP(String table) {
        List<String> list = getIPList(table);
        return list.isEmpty() ? null : list.get(0); // 默认取第一个 IP
    }

    // 判断是否有缓存该表
    public boolean contains(String table) {
        return tableToIPs.containsKey(table.toLowerCase());
    }

    // 判断某个表是否缓存了指定 IP
    public boolean containsIP(String table, String ip) {
        Set<String> set = tableToIPs.get(table.toLowerCase());
        return set != null && set.contains(ip);
    }

    // 移除某张表的缓存
    public void remove(String table) {
        tableToIPs.remove(table.toLowerCase());
    }

    // 打印所有缓存
    public void printCache() {
        tableToIPs.forEach((k, v) -> System.out.println(k + " => " + v));
    }

    // 移除表对应的某个 IP
    public void removeIP(String table, String ip) {
        Set<String> ipSet = tableToIPs.get(table.toLowerCase());
        if (ipSet != null) {
            ipSet.remove(ip);
            // 如果 IP 列表为空，删除整个表项
            if (ipSet.isEmpty()) {
                tableToIPs.remove(table.toLowerCase());
            }
        }
    }

}
