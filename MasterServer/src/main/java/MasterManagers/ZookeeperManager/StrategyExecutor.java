package MasterManagers.ZookeeperManager;
import MasterManagers.SocketManager.SocketThread;
import MasterManagers.TableManager;

import java.util.ArrayList;
import java.util.List;

public class StrategyExecutor {
    private TableManager tableManager;

    public StrategyExecutor(TableManager tableManager) {
        this.tableManager = tableManager;
    }

    // 检查当前主节点是否存储了某个ip地址的已运行服务
    public boolean existServer(String hostUrl) {
        return tableManager.isExistServer(hostUrl);
    }

    public void execStrategy(String hostUrl, StrategyTypeEnum type) {
        try {
            switch (type) {
                case RECOVER:
                    execRecoverStrategy(hostUrl);
                    break;
                case DISCOVER:
                    execDiscoverStrategy(hostUrl);
                    break;
                case INVALID:
                    execInvalidStrategy(hostUrl);
                    break;
            }
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
            System.out.println("执行策略失败: " + e.getMessage());
        }
    }

    private void execInvalidStrategy (String hostUrl) {
        StringBuilder allTable = new StringBuilder();
        List<String> tableList = tableManager.getTableList(hostUrl);
        //<master>[3]ip#name@name@
        String bestInet = tableManager.getBestServer(hostUrl);
//        log.warn("bestInet:{}", bestInet);
        if (bestInet == null) {
            System.out.println("没有找到可用的服务器,负载均衡失败");
            return;
        }
        System.out.println("bestInet: " + bestInet);
        allTable.append(hostUrl).append("#");
        int i = 0;
        for(String s:tableList){
            allTable.append(s);
        }
        tableManager.transferTables(hostUrl,bestInet);
        SocketThread socketThread = tableManager.getSocketThread(bestInet);
        socketThread.sendCommand("[3]"+allTable);
    }

    private void execDiscoverStrategy(String hostUrl) {
        // 生成一个新的表，为空
        List<String> tableList = new ArrayList<>();
        tableManager.addTables(tableList, hostUrl);
    }

    private void execRecoverStrategy(String hostUrl) {
        // 生成一个新的表，为空
        List<String> tableList = new ArrayList<>();
        tableManager.recoverServer(hostUrl,tableList);
        SocketThread socketThread = tableManager.getSocketThread(hostUrl);
        socketThread.sendCommand("[4]recover");
    }
}
