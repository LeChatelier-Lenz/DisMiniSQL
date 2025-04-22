import config.TableLocationCache;
import network.MasterClient;
import network.SlaveClient;
import utils.SQLParser;

import java.util.Scanner;
public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TableLocationCache cache = new TableLocationCache();

        String masterIP = "127.0.0.1";
        int masterPort = 8888;
        MasterClient masterClient = new MasterClient(masterIP, masterPort);
        SlaveClient slaveClient = new SlaveClient();

        System.out.println("MiniSQL 分布式客户端启动");

        while (true) {
            System.out.print("MiniSQL> ");
            String sql = scanner.nextLine().trim();

            if (sql.equalsIgnoreCase("exit")) break;

            String tableName = SQLParser.extractTableName(sql);
            if (tableName == null) {
                System.out.println("无法识别 SQL 中的表名！");
                continue;
            }

            String targetIP;
            if (cache.contains(tableName)) {
                targetIP = cache.getIP(tableName);
                System.out.println("从缓存中找到表 " + tableName + " 位于 " + targetIP);
            } else {
                // 向主节点请求该表所在从节点的 IP
                String response = masterClient.sendToMaster("TABLE_LOOKUP " + tableName);
                if (response.startsWith("ERROR")) {
                    System.out.println("主节点返回错误：" + response);
                    continue;
                }
                targetIP = response.trim();
                cache.cache(tableName, targetIP);
                System.out.println("主节点返回表位置，已缓存：" + tableName + " -> " + targetIP);
            }

            // 发送 SQL 到对应的从节点
            String[] ipParts = targetIP.split(":");
            String ip = ipParts[0];
            int port = Integer.parseInt(ipParts[1]);

            String result = slaveClient.sendToSlave(ip, port, sql);
            System.out.println("查询结果：\n" + result);
        }

        scanner.close();
    }
}
