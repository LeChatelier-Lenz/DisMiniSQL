package MasterManagers.ZookeeperManager;
import MasterManagers.Utils.SocketUtils;


import MasterManagers.TableManager;
import org.apache.zookeeper.CreateMode;

/**
 * ZooKeeper的管理器，主要用于连接ZooKeeper集群，并创建ZooKeeper客户端
 */
public class ZookeeperManager implements Runnable{
    private TableManager tableManager;
    private final int TaskType;

    // ZooKeeper集群访问的端口
    //public static final String ZK_HOST = "localhost:2181";
    public static final String ZK_HOST = "10.162.234.78:2181"; // 本机的对外WLAN IP地址

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


    // 主节点调用
    public ZookeeperManager(TableManager tableManager) {
        this.tableManager = tableManager;
        this.TaskType = 0;

    }

    // 从节点简单调用
    public ZookeeperManager() {
        this.tableManager = null;
        this.TaskType = 1;
//        ZK_HOST = "host.docker.internal:2181";
    }



    public void run(){
        if(TaskType == 0){
            this.startZookeeperService();
        }else{
            this.serviceRegister();
        }
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
//            log.warn(e.getMessage(),e);
            System.out.println("ZooKeeper服务启动失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void serviceRegister() {
        try {
            // 向ZooKeeper注册临时节点
            CuratorClient curatorClient = new CuratorClient(ZK_HOST);
            int nChildren = curatorClient.getNodeChildren(ZookeeperManager.ZNODE).size();
            if(nChildren==0)
                curatorClient.createNode(getRegisterPath() + nChildren, SocketUtils.getHostAddress(), CreateMode.EPHEMERAL);
            else{
                String index = String.valueOf(Integer.parseInt((curatorClient.getNodeChildren(ZookeeperManager.ZNODE)).get(nChildren - 1).substring(7)) + 1);
                curatorClient.createNode(getRegisterPath() + index, SocketUtils.getHostAddress(), CreateMode.EPHEMERAL);
            }

            // 阻塞该线程，直到发生异常或者主动退出
            synchronized (this) {
                wait();
            }
        } catch (Exception e) {
//            log.warn(e.getMessage(), e);
            System.out.println("ZooKeeper服务注册失败: " + e.getMessage());
            e.printStackTrace();

        }
    }

    /**
     * @description: 获取Zookeeper注册的路径
     */
    private static String getRegisterPath() {
        return ZookeeperManager.ZNODE + "/" + ZookeeperManager.HOST_NAME_PREFIX;
    }

}
