package config;

import java.util.concurrent.ConcurrentHashMap;

public class TableLocationCache {
    private final ConcurrentHashMap<String, String> tableToIP = new ConcurrentHashMap<>();

    // 缓存表名对应IP
    public void cache(String table, String ip) {
        tableToIP.put(table.toLowerCase(), ip);
    }

    // 获取表对应的IP
    public String getIP(String table) {
        return tableToIP.get(table.toLowerCase());
    }

    // 判断是否缓存了表
    public boolean contains(String table) {
        return tableToIP.containsKey(table.toLowerCase());
    }

    // 移除表名对应缓存（用于 DROP 表）
    public void remove(String table) {
        tableToIP.remove(table.toLowerCase());
    }

    // 打印当前缓存
    public void printCache() {
        tableToIP.forEach((k, v) -> System.out.println(k + " => " + v));
    }
}
