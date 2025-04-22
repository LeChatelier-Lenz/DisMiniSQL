package config;

import java.util.concurrent.ConcurrentHashMap;

public class TableLocationCache {
    private final ConcurrentHashMap<String, String> tableToIP = new ConcurrentHashMap<>();

    public void cache(String table, String ip) {
        tableToIP.put(table.toLowerCase(), ip);
    }

    public String getIP(String table) {
        return tableToIP.get(table.toLowerCase());
    }

    public boolean contains(String table) {
        return tableToIP.containsKey(table.toLowerCase());
    }

    public void printCache() {
        tableToIP.forEach((k, v) -> System.out.println(k + " => " + v));
    }
}
