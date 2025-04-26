package MasterManagers;

import java.io.IOException;
import MasterManagers.SocketManager.SocketManager;
import MasterManagers.ZookeeperManager.ZookeeperManager;

/**
 *  MasterManager类负责管理主节点的所有操作，包括与ZooKeeper的交互和与从节点的通信。
 *  它包含了一个TableManager实例，用于管理表的元数据。
 *  它还包含了一个SocketManager实例，用于处理与从节点之间的通信。
 *  它的构造函数初始化了这些实例，并且在initialize方法中启动了两个线程。

 */
public class MasterManager {
    private TableManager tableManager;
    private ZookeeperManager zookeeperManager;
    private SocketManager socketManager;

    // 默认主节点的端口号
    private final int PORT = 12345;


    public MasterManager() throws IOException, InterruptedException {
        tableManager = new TableManager();
        zookeeperManager = new ZookeeperManager(tableManager);
        socketManager = new SocketManager(PORT, tableManager);
    }


    /**
     * MasterManager的初始化方法
     * @throws InterruptedException
     * @throws IOException
     */
    public void initialize() throws InterruptedException, IOException {
        // 第一个线程在启动时向ZooKeeper发送请求，获得ZNODE目录下的信息并且持续监控，如果发生了目录的变化则执行回调函数，处理相应策略。
        Thread zkServiceThread = new Thread(zookeeperManager);
        zkServiceThread.start();

        // 第二个线程负责处理与从节点之间的通信，以及响应客户端的请求
        socketManager.startService();
    }
}
