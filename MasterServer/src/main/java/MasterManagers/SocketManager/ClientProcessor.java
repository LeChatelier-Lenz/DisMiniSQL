package MasterManagers.SocketManager;

import java.net.Socket;
import java.util.List;

import MasterManagers.TableManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientProcessor {

    private final TableManager tableManager;
    private final Socket socket; // 用不到这个变量

    public ClientProcessor(TableManager _tableManager, Socket _socket) {
        this.tableManager = _tableManager;
        this.socket = _socket;
    }

    public String processClientCommand(String cmd) {
        String result = "";
        String tableName = cmd.substring(3);
        if (cmd.startsWith("[1]")) {
            // <client>[1]tableName 查询表所在所有节点
            List<String> IPs = this.tableManager.getRegionIPs(tableName);
            if (IPs != null) {
                result = "[1]" + String.join(",", IPs);
                log.info("客户端查询表 {} 所在节点，返回IP列表: {}", tableName, String.join(",", IPs));
            } else {
                result = "[1]127.0.0.1:9999";
                log.info("未查询到表: {}", tableName);
            }

        } else if (cmd.startsWith("[2]")) {
            // <client>[2]tableName 创建新表，返回创建节点IP
            String IP = this.tableManager.getBestServer();
            this.tableManager.addTable(tableName, IP);
            result = "[2]" + IP;
            log.info("客户端创建表 {}，分配到节点 {}", tableName, IP);
        } else if (cmd.startsWith("[3]")) {
            // <client>[3]tableName 删除所有节点上的该表
            boolean res = this.tableManager.deleteTableFromAllServers(tableName);
            result = "[3]" + (res ? "OK" : "FAIL");
            if (res) {
                log.info("客户端删除表 {} 成功", tableName);
            } else {
                log.warn("客户端删除表 {} 失败，表可能不存在", tableName);
            }
        } else {
            log.warn("收到无效的客户端命令格式: {}", cmd);
        }
        return result;
    }
}
