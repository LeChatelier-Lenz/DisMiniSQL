import config.TableLocationCache;
import network.MasterClient;
import network.SlaveClient;
import utils.SQLParser;
import utils.SQLParser.SQLInfo;
import utils.SQLParser.SQLType;

import java.util.*;

public class Client {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TableLocationCache cache = new TableLocationCache();

        String masterIP = "10.192.31.16";
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

            try {
                switch (info.type) {
                    case CREATE: {
                        String resp = masterClient.sendToMaster("<client>[4]" + tableName);
                        if (resp.startsWith("<master>[4]")) {
                            List<String> ipList = Arrays.asList(resp.substring(11).split(","));
                            List<String> outputs = new ArrayList<>();
                            for (String ip : ipList) {
                                ip = ip.trim();
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    outputs.add("[" + ip + "] " + result);
                                    cache.add(tableName, ip);
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    outputs.add("[" + ip + "] 连接失败，已移除缓存");
                                }
                            }
                            outputs.forEach(System.out::println);
                        } else {
                            System.out.println("主节点返回错误：" + resp);
                        }
                        break;
                    }

                    case SELECT: {
                        boolean retried = false;
                        System.out.println("开始执行select");
                        while (true) {
                            List<String> ipList = cache.getIPList(tableName);

                            if (ipList == null || ipList.isEmpty()) {
                                System.out.println("IP列表为空，向主节点请求IP");
                                String resp = masterClient.sendToMaster("<client>[4]" + tableName);
                                if (resp.startsWith("<master>[4]")) {
                                    List<String> newIPs = Arrays.asList(resp.substring(11).split(","));
                                    cache.replace(tableName, newIPs);
                                    ipList = newIPs;
                                } else {
                                    System.out.println("主节点返回错误：" + resp);
                                    break;
                                }
                            }

                            List<String> outputs = new ArrayList<>();
                            boolean failed = false;

                            for (String ip : ipList) {
                                try {
                                    System.out.println("开始尝试第一个节点发送");
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    outputs.add("[" + ip + "] " + result);
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    failed = true;
                                    break;
                                }
                            }

                            if (!failed) {
                                outputs.forEach(System.out::println);
                                break;
                            } else if (!retried) {
                                String resp = masterClient.sendToMaster("<client>[4]" + tableName);
                                if (resp.startsWith("<master>[4]")) {
                                    List<String> newIPs = Arrays.asList(resp.substring(11).split(","));
                                    cache.replace(tableName, newIPs);
                                    retried = true;
                                } else {
                                    System.out.println("主节点返回错误：" + resp);
                                    break;
                                }
                            } else {
                                System.out.println("所有节点连接失败");
                                break;
                            }
                        }
                        break;
                    }

                    case INSERT: {
                        boolean retried = false;
                        while (true) {
                            String resp = masterClient.sendToMaster("<client>[1]" + tableName);
                            if (resp.startsWith("<master>[1]")) {
                                String ip = resp.substring(11).trim();
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    System.out.println("[" + ip + "] " + result);
                                    cache.add(tableName, ip);
                                    break;
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    if (!retried) {
                                        retried = true;
                                    } else {
                                        System.out.println("连接失败，插入终止");
                                        break;
                                    }
                                }
                            } else {
                                System.out.println("主节点返回错误：" + resp);
                                break;
                            }
                        }
                        break;
                    }

                    case DELETE: {
                        boolean retried = false;

                        while (true) {
                            List<String> ipList = cache.getIPList(tableName);
                            List<String> outputs = new ArrayList<>();
                            boolean failed = false;

                            for (String ip : ipList) {
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    outputs.add("[" + ip + "] " + result);
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    failed = true;
                                    break;
                                }
                            }

                            if (!failed) {
                                outputs.forEach(System.out::println);
                                break;
                            } else if (!retried) {
                                String resp = masterClient.sendToMaster("<client>[4]" + tableName);
                                if (resp.startsWith("<master>[4]")) {
                                    List<String> newIPs = Arrays.asList(resp.substring(11).split(","));
                                    cache.replace(tableName, newIPs);
                                    retried = true;
                                } else {
                                    System.out.println("主节点返回错误：" + resp);
                                    break;
                                }
                            } else {
                                System.out.println("所有节点连接失败，删除终止");
                                break;
                            }
                        }
                        break;
                    }

                    case DROP: {
                        List<String> ipList = cache.getIPList(tableName);
                        List<String> outputs = new ArrayList<>();

                        for (String ip : ipList) {
                            try {
                                String result = slaveClient.sendToSlave(ip, 22222, sql);
                                outputs.add("[" + ip + "] " + result);
                            } catch (Exception e) {
                                cache.removeIP(tableName, ip);
                                outputs.add("[" + ip + "] 无法连接，移除缓存");
                            }
                        }

                        cache.remove(tableName);
                        outputs.forEach(System.out::println);
                        break;
                    }

                    default:
                        System.out.println("该类型操作暂未实现！");
                }
            } catch (Exception e) {
                System.out.println("执行出错：" + e.getMessage());
            }
        }

        scanner.close();
    }
}
