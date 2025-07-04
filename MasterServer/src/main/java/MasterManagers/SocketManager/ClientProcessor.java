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
            String IPs = this.tableManager.getNextRegionIP(tableName);
            if (tableName == null) {
                result = "[1]null";
                log.info("客户端查询表 {} 所在节点，未搜索到IP", tableName);
            } else {
                result = "[1]" + String.join(",", IPs);
                log.info("客户端查询表 {} 所在节点，返回IP列表: {}", tableName, IPs);
            }
        } else if (cmd.startsWith("[2]")) {
            // <client>[2]tableName 创建新表，返回创建节点IP
            // 这个只在一个节点上创建表 现废弃
            String IP = this.tableManager.getBestServer();
            if (IP.equals("")) {
                result = "[2]null";
                log.info("客户端创建表 {}，未找到可以分配的节点", tableName);
            } else {
                result = "[2]" + IP;
                log.info("客户端创建表 {}，分配到节点 {}", tableName, IP);
            }
        } else if (cmd.startsWith("[3]")) {
            // <client>[3]tableName 删除所有节点上的该表
            boolean res = this.tableManager.deleteTableFromAllServers(tableName);
            result = "[3]" + (res ? "OK" : "FAIL");
            if (res) {
                log.info("客户端删除表 {} 操作发送成功", tableName);
            } else {
                log.warn("客户端删除表 {} 操作发送失败，表可能不存在", tableName);
            }
        } else if (cmd.startsWith("[4]")) {
            // <client>[4] 获取当前所有活跃节点
            List<String> IPs = this.tableManager.getAllAliveIPs();
            if (IPs.isEmpty()) {
                result = "[4]";
                log.warn("现无活跃子节点");
            } else {
                result = "[4]" + String.join(",", IPs);
                log.info("返回现活跃子节点: {}", IPs);
            }
        } else {
            log.warn("收到无效的客户端命令格式: {}", cmd);
        }
        return result;
    }
}
