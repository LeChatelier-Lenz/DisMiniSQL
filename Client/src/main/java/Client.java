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

        String masterIP = "10.162.234.78";
        int masterPort = 12345;
        MasterClient masterClient = new MasterClient(masterIP, masterPort);
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
                case INSERT:
                case UPDATE:
                case DELETE:
                    // 查询表所在节点（如果缓存中没有）
                    if (cache.contains(tableName)) {
                        targetIP = cache.getIP(tableName);
                        System.out.println("从缓存中找到表 " + tableName + " 位于 " + targetIP);
                    } else {
                        masterRequest = "<client>[1]" + tableName;
                        String response = masterClient.sendToMaster(masterRequest);
                        System.out.println("主节点返回内容" + response);
                        if (response.startsWith("<master>[1]")) {
                            targetIP = response.substring(11).split(",")[0].trim(); // 默认取第一个IP
                            cache.cache(tableName, targetIP);
                            System.out.println("主节点返回表位置，已缓存：" + tableName + " -> " + targetIP);
                        } else {
                            System.out.println("主节点返回错误：" + response);
                            continue;
                        }
                    }
                    break;

                case CREATE:
                    masterRequest = "<client>[2]" + tableName;
                    String createResp = masterClient.sendToMaster(masterRequest);
                    if (createResp.startsWith("<master>[2]")) {
                        targetIP = createResp.substring(11).trim();
                        cache.cache(tableName, targetIP);
                        System.out.println("主节点分配创建表到：" + targetIP);
                    } else {
                        System.out.println("主节点返回错误：" + createResp);
                        continue;
                    }
                    break;
                case DROP:
                    if (cache.contains(tableName)) {
                        targetIP = cache.getIP(tableName);
                        System.out.println("从缓存中找到表 " + tableName + " 位于 " + targetIP);
                    } else {
                        masterRequest = "<client>[1]" + tableName;
                        String response = masterClient.sendToMaster(masterRequest);
                        System.out.println("主节点返回内容" + response);
                        if (response.startsWith("<master>[1]")) {
                            targetIP = response.substring(11).split(",")[0].trim(); // 默认取第一个IP
                            cache.remove(tableName);
                            System.out.println("主节点返回表位置，已缓存：" + tableName + " -> " + targetIP);
                        } else {
                            System.out.println("主节点返回错误：" + response);
                            continue;
                        }
                    }
                    break;
            }

            // 向从节点发送 SQL
            try {
                String[] ipParts = targetIP.split(":");
                //String ip = ipParts[0];
//                String ip = "10.162.2.153";
                String ip = "10.162.181.29";
                int port = 22222;
                System.out.println("尝试向从节点 " + ip + ":" + port + " 发送 SQL：" + sql);
                String result = slaveClient.sendToSlave(ip, port, sql);
                System.out.println("运行结果：");
                System.out.println(result);

            } catch (Exception e) {
                System.out.println("发送 SQL 到从节点失败：" + e.getMessage());
                // 如果连接失败，从缓存中删除该表的信息
                if (cache.contains(tableName)) {
                    cache.remove(tableName);
                    System.out.println("从缓存中移除表 " + tableName + " 的信息，因为从节点 " + targetIP + " 已掉线。");
                }
                // 重新向主节点发送请求，获取新的表位置信息
                masterRequest = "<client>[1]" + tableName;
                String response = masterClient.sendToMaster(masterRequest);
                System.out.println("主节点返回内容：" + response);
                if (response.startsWith("<master>[1]")) {
                    targetIP = response.substring(11).split(",")[0].trim(); // 默认取第一个IP
                    cache.cache(tableName, targetIP);
                    System.out.println("主节点返回表位置，已缓存：" + tableName + " -> " + targetIP);
                    // 再次尝试向从节点发送 SQL
                    try {
                        String[] ipParts = targetIP.split(":");
                        String ip = ipParts[0];
                        //String ip = "10.162.181.29";
                        int port = 22222;
                        System.out.println("重新尝试向从节点 " + ip + ":" + port + " 发送 SQL：" + sql);
                        String result = slaveClient.sendToSlave(ip, port, sql);
                        System.out.println("运行结果：");
                        System.out.println(result);
                    } catch (Exception e2) {
                        System.out.println("再次发送 SQL 到从节点失败：" + e2.getMessage());
                    }
                } else {
                    System.out.println("主节点返回错误：" + response);
                }
            }
        }

        scanner.close();
    }
}