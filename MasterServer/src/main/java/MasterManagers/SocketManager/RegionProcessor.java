package MasterManagers.SocketManager;

import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import MasterManagers.TableManager;
import MasterManagers.Utils.SocketUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
        if (IP.equals("127.0.0.1")) {
            IP = SocketUtils.getHostAddress();
        }

        if (cmd.startsWith("[1]")) {
            // <region>[1]tableName1 tableName2 ...
            // 处理从节点启动时上报本地存储的所有表名
            cmd = cmd.substring(3);
            List<String> tableNames = Arrays.asList(cmd.split("\\s+"));

            boolean success = this.tableManger.addTables(tableNames, IP);
            if (success) {
                log.info("Region服务器 {} 表信息上报成功", IP);
            } else {
                log.warn("Region服务器 {} 表信息上报失败，可能服务器状态异常", IP);
            }
        } else if (cmd.startsWith("[2]")) {
            // <region>[2]tableName ADD/DEL
            // 处理从节点表创建/删除通知
            cmd = cmd.substring(3).trim();
            String[] parts = cmd.split("\\s+", 2);
            if (parts.length == 2) {
                String tableName = parts[0];
                String operation = parts[1];

                if ("ADD".equalsIgnoreCase(operation)) {
                    // 添加表
                    this.tableManger.addTable(tableName, IP);
                    log.info("Region服务器 {} 添加表 {} 成功", IP, tableName);
                } else if ("DEL".equalsIgnoreCase(operation)) {
                    // 删除表
                    boolean success = this.tableManger.deleteTable(tableName, IP);
                    if (success) {
                        log.info("Region服务器 {} 删除表 {} 成功", IP, tableName);
                    } else {
                        log.warn("Region服务器 {} 删除表 {} 失败，表不存在或服务器不包含该表", IP, tableName);
                    }
                } else {
                    log.warn("Region命令[2]操作类型错误: {}", operation);
                }
            } else {
                log.warn("Region命令[2]格式错误: {}", cmd);
            }
        } else if (cmd.startsWith("[3]")) {
            // <region>[3]Complete disaster recovery
            // 处理从节点完成故障恢复通知
            if ("Complete disaster recovery".equalsIgnoreCase(cmd.substring(3).trim())) {
                this.tableManger.recoverServer(IP, new ArrayList<>());
                log.info("Region服务器 {} 已完成故障恢复", IP);
                // 可以在这里添加其他恢复完成后的处理逻辑
            } else {
                log.warn("Region命令[3]格式错误: {}", cmd);
            }
        } else if (cmd.startsWith("[4]")) {
            // <region>[4]
            // 处理从节点清空数据后的反馈
            if (cmd.substring(3).trim().isEmpty()) {
                log.info("Region服务器 {} 已清空本地数据", IP);
                // 可以在这里添加其他清空数据后的处理逻辑
            } else {
                log.warn("Region命令[4]格式错误: {}", cmd);
            }
        } else {
            log.warn("Region命令格式出错 {}", cmd);
        }
        return result;
    }
}
