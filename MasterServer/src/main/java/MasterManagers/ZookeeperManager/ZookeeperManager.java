package MasterManagers.ZookeeperManager;
import lombok.extern.slf4j.Slf4j;


import MasterManagers.TableManager;
import org.apache.curator.framework.recipes.cache.TreeCache;

/**
 * ZooKeeper的管理器，主要用于连接ZooKeeper集群，并创建ZooKeeper客户端
 */
@Slf4j
public class ZookeeperManager implements Runnable{
    private TableManager tableManager;

    // ZooKeeper集群访问的端口
    public static final String ZK_HOST = "localhost:2181";
    // ZooKeeper会话超时时间
    public static final Integer ZK_SESSION_TIMEOUT_MS = 3000;
    // ZooKeeper连接超时时间
    public static final Integer ZK_CONNECTION_TIMEOUT_MS = 3000;
    // ZooKeeper重试策略，初始等待时间为1000ms，最大重试次数为5次
    public static final int ZK_BASE_SLEEP_TIME_MS = 1000;
    public static final int ZK_MAX_RETRIES = 5;
    //ZooKeeper集群内各个服务器注册的节点路径
    public static final String ZNODE = "/db";
    //ZooKeeper集群内各个服务器注册自身信息的节点名前缀
    public static final String HOST_NAME_PREFIX = "Region_";

    public ZookeeperManager(TableManager tableManager) {
        this.tableManager = tableManager;
    }


    public void run(){
        this.startZookeeperService();
    }


    /**
     * 开启ZooKeeper服务
     */
    public void startZookeeperService(){
        try {
            // 连接到Zookeeper服务器, 并创建一个ZooKeeper客户端
            CuratorClient curatorClient = new CuratorClient(ZK_HOST,tableManager);
            if(!curatorClient.checkNodeExist(ZNODE)){
                // 如果没有主目录，则创建一个主目录节点
                curatorClient.createNode(ZNODE,"服务器主目录");
            }
            // 开始监听服务器目录（的所有子节点），如果有节点的变化，则处理相应事件
            curatorClient.monitorChildrenNodes(ZNODE);
        } catch (Exception e) {
            log.warn(e.getMessage(),e);
        }
    }
}
