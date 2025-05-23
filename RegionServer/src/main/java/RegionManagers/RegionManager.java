package RegionManagers;

import MasterManagers.ZookeeperManager.ZookeeperManager;
import RegionManagers.SockectManager.ClientSocketManager;
import RegionManagers.SockectManager.MasterSocketManager;
import miniSQL.API;
import miniSQL.Interpreter;

import java.io.IOException;

// 整个Region Server的manager
public class RegionManager {
    private DataBaseManager dataBaseManager;
    private ClientSocketManager clientSocketManager;
    private MasterSocketManager masterSocketManager;
    private ZookeeperManager zkServiceManager;

    private final int PORT = 22222;

    public RegionManager() throws IOException, InterruptedException {
        dataBaseManager = new DataBaseManager();
        masterSocketManager = new MasterSocketManager();
        zkServiceManager = new ZookeeperManager(masterSocketManager.getMyIP());
        masterSocketManager.sendTableInfoToMaster(dataBaseManager.getMetaInfo());
        clientSocketManager = new ClientSocketManager(PORT, masterSocketManager);

        // 测试代码，测试region和master的沟通情况
//        masterSocketManager.sendToMaster(dataBaseManager.getMetaInfo());
//        Thread masterThread = new Thread(masterSocketManager);
//        masterThread.start();
    }

    public void run() throws Exception {
        // 线程1：在应用启动的时候自动将本机的Host信息注册到ZooKeeper，然后阻塞，直到应用退出的时候也同时退出

        API.initial();
        Thread zkServiceThread = new Thread(zkServiceManager);
        zkServiceThread.start();
        Thread MasterSocketThread = new Thread(masterSocketManager);
        MasterSocketThread.start();
        Thread centerThread = new Thread(clientSocketManager);
        centerThread.start();

        System.out.println("从节点开始运行！");
    }
}