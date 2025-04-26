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
            result = "[1]" + String.join(",", IPs);
        } else if (cmd.startsWith("[2]")) {
            // <client>[2]tableName 创建新表，返回创建节点IP
            String IP = this.tableManager.getBestServer();
            this.tableManager.addTable(tableName, IP);
            result = "[2]" + IP;
        } else if (cmd.startsWith("[3]")) {
            boolean res = this.tableManager.deleteTableFromAllServers(tableName);
            result = "[3]" + (res ? "OK" : "FAIL");
        } else {
            log.warn("Client命令格式出错 {}", cmd);
        }
        return result;
    }
}
