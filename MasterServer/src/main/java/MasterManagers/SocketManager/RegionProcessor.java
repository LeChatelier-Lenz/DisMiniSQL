package MasterManagers.SocketManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import MasterManagers.TableManager;
import MasterManagers.Utils.SocketUtils;

public class RegionProcessor {

    private final TableManager tableManger;
    private final Socket socket;

    public RegionProcessor(TableManager _tableManger, Socket _socket) {
        this.tableManger = _tableManger;
        this.socket = _socket;
    }

    public String processRegionCommand(String cmd) {
        String result = "";
        String IP = socket.getInetAddress().getHostAddress();
        String hostname = socket.getInetAddress().getHostName();
        int port = socket.getPort();

        // Log both IP and hostname for better identification
        System.out.println("RegionProcessor: IP=" + IP + ", Hostname=" + hostname + ", Port=" + port);

        if (cmd.startsWith("[1]")) {
            cmd = cmd.substring(3).trim();
            String identifier = !hostname.equals(IP) ? hostname : IP;

            // 如果不存在表名，则说明只是简单的socket连接，节点注册交给ZookeeperManager，此处不处理
            if(cmd.isEmpty()) {
                System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 准备注册");
                return result;
            }

            List<String> tableNames = Arrays.asList(cmd.split("\\s+"));
            boolean success = this.tableManger.addTables(tableNames, IP);
            if (success) {
                System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 表信息上报成功");
            } else {
                System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 表信息上报失败，可能服务器状态异常");
            }
        } else if (cmd.startsWith("[2]")) {
            cmd = cmd.substring(3).trim();
            String[] parts = cmd.split("\\s+", 2);
            if (parts.length == 2) {
                String tableName = parts[0];
                String operation = parts[1];
                String identifier = !hostname.equals(IP) ? hostname : IP;

                if ("ADD".equalsIgnoreCase(operation)) {
                    this.tableManger.addTable(tableName, IP);
                    System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 添加表 " + tableName + " 成功");
                } else if ("DEL".equalsIgnoreCase(operation)) {
                    boolean success = this.tableManger.deleteTable(tableName, IP);
                    if (success) {
                        System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 删除表 " + tableName + " 成功");
                    } else {
                        System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 删除表 " + tableName + " 失败，表不存在或服务器不包含该表");
                    }
                } else {
                    System.out.println("Region命令[2]操作类型错误: " + operation);
                }
            } else {
                System.out.println("Region命令[2]格式错误: " + cmd);
            }
        } else if (cmd.startsWith("[3]")) {
            if ("Complete disaster recovery".equalsIgnoreCase(cmd.substring(3).trim())) {
                String identifier = !hostname.equals(IP) ? hostname : IP;
                this.tableManger.recoverServer(IP, new ArrayList<>());
                System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 已完成故障恢复");
            } else {
                System.out.println("Region命令[3]格式错误: " + cmd);
            }
        } else if (cmd.startsWith("[4]")) {
            if (cmd.substring(3).trim().isEmpty()) {
                String identifier = !hostname.equals(IP) ? hostname : IP;
                System.out.println("Region服务器 " + identifier + " (IP: " + IP + ") 已清空本地数据");
            } else {
                System.out.println("Region命令[4]格式错误: " + cmd);
            }
        } else {
            System.out.println("Region命令格式出错 " + cmd);
        }
        return result;
    }
}