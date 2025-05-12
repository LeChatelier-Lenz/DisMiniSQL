import config.TableLocationCache;
import network.MasterClient;
import network.SlaveClient;
import utils.SQLParser;
import utils.SQLParser.SQLInfo;
import utils.SQLParser.SQLType;

import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TableLocationCache cache = new TableLocationCache();

<<<<<<< HEAD
        String masterIP = "10.192.211.225";
        int masterPort = 12345;
        MasterClient masterClient = new MasterClient(masterIP, masterPort,10000);
=======
        String masterIP = "127.0.0.1";
        int masterPort = 8888;
        MasterClient masterClient = new MasterClient(masterIP, masterPort);
>>>>>>> 04dfbebd3d1c9ac5bed4c30a7cc5396492c3bfd4
        SlaveClient slaveClient = new SlaveClient();

        System.out.println("MiniSQL 分布式客户端启动");

        while (true) {
            System.out.print("MiniSQL> ");
            String sql = scanner.nextLine().trim();
            if (sql.equalsIgnoreCase("exit")) break;

            SQLInfo info = SQLParser.parse(sql);
            if (info.type == SQLType.UNKNOWN || info.tableName == null) {
                System.out.println("无法识别 SQL 类型或表名！");
                continue;
            }

            String tableName = info.tableName;
            String targetIP = null;
            String masterRequest = null;

            switch (info.type) {
                case SELECT:
                    masterRequest = "<region>[1]"+"users";
                    String response = masterClient.sendToMaster(masterRequest);
                case INSERT:
                case UPDATE:
                case DELETE:
                    // 查询表所在节点（如果缓存中没有）
                    if (cache.contains(tableName)) {
                        targetIP = cache.getIP(tableName);
                        System.out.println("从缓存中找到表 " + tableName + " 位于 " + targetIP);
                    } else {
<<<<<<< HEAD
                        masterRequest = "<client>[1]" + tableName;
                        response = masterClient.sendToMaster(masterRequest);
                        System.out.println("主节点返回内容" + response);
                        if (response.startsWith("<master>[1]")) {
                            targetIP = response.substring(11).split(",")[0].trim(); // 默认取第一个IP
=======
                        masterRequest = "[1]" + tableName;
                        String response = masterClient.sendToMaster(masterRequest);
                        if (response.startsWith("Master [1]")) {
                            targetIP = response.substring(3).split(",")[0].trim(); // 默认取第一个IP
>>>>>>> 04dfbebd3d1c9ac5bed4c30a7cc5396492c3bfd4
                            cache.cache(tableName, targetIP);
                            System.out.println("主节点返回表位置，已缓存：" + tableName + " -> " + targetIP);
                        } else {
                            System.out.println("主节点返回错误：" + response);
                            continue;
                        }
                    }
                    break;

                case CREATE:
<<<<<<< HEAD
                    masterRequest = "<client>[2]" + tableName;
                    String createResp = masterClient.sendToMaster(masterRequest);
                    if (createResp.startsWith("<master>[2]")) {
                        targetIP = createResp.substring(11).trim();
=======
                    masterRequest = "[2]" + tableName;
                    String createResp = masterClient.sendToMaster(masterRequest);
                    if (createResp.startsWith("[2]")) {
                        targetIP = createResp.substring(3).trim();
>>>>>>> 04dfbebd3d1c9ac5bed4c30a7cc5396492c3bfd4
                        cache.cache(tableName, targetIP);
                        System.out.println("主节点分配创建表到：" + targetIP);
                    } else {
                        System.out.println("主节点返回错误：" + createResp);
                        continue;
                    }
                    break;

                case DROP:
<<<<<<< HEAD
                    masterRequest = "<client>[3]" + tableName;
                    String dropResp = masterClient.sendToMaster(masterRequest);
                    if (dropResp.startsWith("<master>[3]")) {
                        String status = dropResp.substring(11).trim();
=======
                    masterRequest = "[3]" + tableName;
                    String dropResp = masterClient.sendToMaster(masterRequest);
                    if (dropResp.startsWith("[3]")) {
                        String status = dropResp.substring(3).trim();
>>>>>>> 04dfbebd3d1c9ac5bed4c30a7cc5396492c3bfd4
                        System.out.println("删除结果：" + status);
                        cache.remove(tableName);
                    } else {
                        System.out.println("主节点返回错误：" + dropResp);
                    }
                    continue; // DROP 不需要发到从节点了
            }

            // 向从节点发送 SQL
            try {
                String[] ipParts = targetIP.split(":");
                String ip = ipParts[0];
                int port = Integer.parseInt(ipParts[1]);
                String result = slaveClient.sendToSlave(ip, port, sql);
                System.out.println("查询结果：\n" + result);
            } catch (Exception e) {
                System.out.println("发送 SQL 到从节点失败：" + e.getMessage());
            }
        }

        scanner.close();
    }
}