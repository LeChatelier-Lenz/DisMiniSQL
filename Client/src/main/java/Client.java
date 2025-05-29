import config.TableLocationCache;
import network.MasterClient;
import network.SlaveClient;
import utils.SQLParser;
import utils.SQLParser.SQLInfo;
import utils.SQLParser.SQLType;

import java.util.*;

public class Client {
    private String extractTableData(String result) {
        if (result == null || result.isEmpty()) {
            return null;
        }
        // 分割结果，提取表格数据部分
        String[] parts = result.split("-->Query");
        if (parts.length > 0) {
            String tablePart = parts[0].trim();
            // 进一步处理，提取表格内容
            if (tablePart.contains("|")) {
                // 找到表格的开始位置（即第一个“|”出现的位置）
                int startIdx = tablePart.indexOf('|');
                if (startIdx != -1) {
                    // 从第一个“|”开始提取表格内容
                    return tablePart.substring(startIdx).trim();
                }
            }
        }
        return null;
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        TableLocationCache cache = new TableLocationCache();

        String masterIP = "10.192.114.34";
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
                        boolean failed = false;
                        if (resp.startsWith("<master>[4]")) {
                            List<String> ipList = Arrays.asList(resp.substring(11).split(","));
                            for (String ip : ipList) {
                                ip = ip.trim();
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    if(result.equals("success")){

                                    }
                                    else{
                                        System.out.println("创建失败:"+result);
                                        failed = true;
                                        break;
                                    }
                                    cache.add(tableName, ip);
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                }
                            }
                            if(!failed){
                                System.out.println("Create table "+info.tableName+" successfully!");
                            }
                        } else {
                            System.out.println("主节点返回错误：" + resp);
                        }
                        break;
                    }

                    case SELECT: {
                        boolean retried = false;

                        while (true) {
                            List<String> ipList = cache.getIPList(tableName);

                            if (ipList == null || ipList.isEmpty()) {
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

                            Set<String> dataRows = new LinkedHashSet<>();  // 用于合并所有节点数据并去重
                            List<String> header = new ArrayList<>();
                            boolean hasAnyData = false;
                            boolean failed = false;

                            for (String ip : ipList) {
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    String[] lines = result.split("\n");
                                    boolean inData = false;

                                    for (int i = 0; i < lines.length; i++) {
                                        String line = lines[i].trim();

                                        if (line.startsWith("-->Query ok! 0 rows")) {
                                            continue; // 无数据，跳过
                                        }

                                        if (line.startsWith("-->Query")) {
                                            break; // 数据段结束
                                        }

                                        if (line.matches("^-+\\s*$")) {
                                            inData = true;
                                            if (header.isEmpty() && i >= 1) {
                                                // 前一行为表头
                                                header.add(lines[i - 1].trim());
                                                header.add(line); // 添加分隔线
                                            }
                                            continue;
                                        }

                                        if (inData && line.matches("^[|].*[|]$")) {
                                            dataRows.add(line); // 添加数据行并自动去重
                                            hasAnyData = true;
                                        }
                                    }
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    failed = true;
                                    break;
                                }
                            }

                            if (!failed) {
                                if (hasAnyData && !header.isEmpty()) {
                                    header.forEach(System.out::println);
                                    dataRows.forEach(System.out::println);
                                } else {
                                    System.out.println("-->Query ok! 0 rows are selected");
                                }
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
                                    //System.out.println("[" + ip + "] " + result);
                                    if(result.equals("success")){
                                        System.out.println("Insert Successfully!");
                                    }
                                    else{
                                        System.out.println(result);
                                    }
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
                            boolean failed = false;
                            boolean output_success = false;

                            for (String ip : ipList) {
                                try {
                                    String result = slaveClient.sendToSlave(ip, 22222, sql);
                                    if(result.equals("success")){
                                        output_success = true;
                                    }
                                } catch (Exception e) {
                                    cache.removeIP(tableName, ip);
                                    failed = true;
                                    break;
                                }
                            }

                            if (!failed) {
                                if(output_success){
                                    System.out.println("Delete Successfully!");
                                }
                                else {
                                    System.out.println("No Data Found!");
                                }
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
                        //List<String> outputs = new ArrayList<>();
                        boolean failed = false;
                        String output = "Drop table "+info.tableName+" successdully!";
                        for (String ip : ipList) {
                            try {
                                String result = slaveClient.sendToSlave(ip, 22222, sql);
                                if(result.equals("success")){

                                }else{
                                    failed = true;
                                    output = result;
                                    break;
                                }
                                //outputs.add("[" + ip + "] " + result);
                            } catch (Exception e) {
                                cache.removeIP(tableName, ip);
                                //outputs.add("[" + ip + "] 无法连接，移除缓存");
                            }
                        }
                        if(!failed){
                            cache.remove(tableName);
                            System.out.println(output);
                        }
                        else{
                            System.out.println(output);
                        }

                        //outputs.forEach(System.out::println);
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
