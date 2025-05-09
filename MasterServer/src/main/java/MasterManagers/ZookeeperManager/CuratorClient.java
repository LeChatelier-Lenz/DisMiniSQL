package MasterManagers.ZookeeperManager;

import MasterManagers.TableManager;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * CuratorClient是一个基于Curator的Zookeeper客户端，提供了对Zookeeper的基本操作
 */
public class CuratorClient {
    /**
     * Zookeeper集群访问的端口，用于节点操作
     */
    private CuratorFramework client = null;

    private TableManager tableManager;

    /**
     * 在注册监听器的时候，如果传入此参数，当事件触发时，逻辑由线程池处理
     */
    private ExecutorService pool = Executors.newFixedThreadPool(2);

    /**
     * Zookeeper集群访问的ip地址:端口号
     */
    private String hostUrl = null;

    /**
     * Zookeeper重试策略，默认为指数回退，初始等待时间为1000ms，最大重试次数为5次
     */
    private RetryPolicy retryPolicy = null;

    public CuratorClient(TableManager tableManager) {
        this.tableManager = tableManager;
        this.setUpConnection(ZookeeperManager.ZK_HOST);
    }

    public CuratorClient(String hostUrl,TableManager tableManager) {
        this.tableManager = tableManager;
        this.setUpConnection(hostUrl);
    }

    /**
     * 不需要传入TableManager的构造函数，用于从节点注册自身信息
     * @param hostUrl zookeeper集群服务器的ip地址:端口号
     */
    public CuratorClient(String hostUrl) {
        this.tableManager = null;
        this.setUpConnection(hostUrl);
    }




    /**
     * 初始化一个基于Curator的Zookeeper客户端，并与特定ip端口的Zookeeper集群相连接
     *
     * @param hostUrl zookeeper集群服务器的ip地址:端口号
     */
    public void setUpConnection(String hostUrl) {
        this.hostUrl = hostUrl;
        this.retryPolicy = new ExponentialBackoffRetry(ZookeeperManager.ZK_BASE_SLEEP_TIME_MS,ZookeeperManager.ZK_MAX_RETRIES);
        if (client == null) {
            synchronized (this) {
                // 创建连接
                client = CuratorFrameworkFactory.builder()
                        .connectString(hostUrl)
                        .connectionTimeoutMs(ZookeeperManager.ZK_CONNECTION_TIMEOUT_MS)
                        .sessionTimeoutMs(ZookeeperManager.ZK_SESSION_TIMEOUT_MS)
                        .retryPolicy(retryPolicy)   // 重试策略：初试时间为1s 重试5次
                        .build();
                client.start();
                System.out.println(client.getState());
            }
        }
    }

    /**
     * 在特定路径上创建节点，并且设定一个特殊的值。默认为持久节点【如果父节点不存在，会先创建父节点】
     *
     * @param nodePath
     * @param value
     * @return
     */
    public void createNode(String nodePath, String value) throws Exception {
        checkConnection();
        client.create().creatingParentsIfNeeded().forPath(nodePath, value.getBytes());
    }


    /**
     * 创建节点
     * @param path 希望创建的节点路径
     * @param value 节点的值
     * @param mode 节点的类型【持久节点、临时节点、顺序节点】
     * @throws Exception
     */
    public void createNode(String path,String value,CreateMode mode) throws Exception {
        checkConnection();
        if(mode == null){
            throw new RuntimeException("创建节点类型不合法");
        }else {
            if (mode == CreateMode.PERSISTENT || mode ==  CreateMode.EPHEMERAL || mode == CreateMode.PERSISTENT_SEQUENTIAL || mode == CreateMode.EPHEMERAL_SEQUENTIAL)  {
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(mode)
                        .forPath(path, value.getBytes());
            }else {
                throw new RuntimeException("不考虑的节点类型");
            }
        }
    }

    /**
     * 检查ZooKeeper连接是否存在，如果不存在则创建默认连接
     */
    public void checkConnection(){
        if (client == null) {
            this.setUpConnection(Objects.requireNonNullElse(hostUrl, ZookeeperManager.ZK_HOST));
        }
    }

    /**
     * 获取指定路径的Zookeeper节点的状态
     * @param nodePath 指定路径
     * @return 节点状态字符串
     * @throws Exception
     */
    public String getNodeState(String nodePath) throws Exception{
        checkConnection();
        Stat status = new Stat();
        client.getData()
                .storingStatIn(status)
                .forPath(nodePath);
        return status.toString();
    }

    /**
     * 获取指定路径的Zookeeper节点的值
     * @param nodePath 指定路径
     * @return 节点值字符串
     * @throws Exception
     */
    public String getNodeValue(String nodePath) throws Exception{
        checkConnection();
        return new String(client.getData().forPath(nodePath));
    }

    /**
     * 获取指定路径的Zookeeper节点的子节点列表
     * @param nodePath 指定路径
     * @return 子节点列表（字符串列表）
     * @throws Exception
     */
    public List<String> getNodeChildren(String nodePath) throws Exception{
        checkConnection();
        return client.getChildren()
                .forPath(nodePath);
    }

    /**
     * 更新指定路径的Zookeeper节点的值
     * @param nodePath 指定路径
     * @param value 新的节点值
     * @throws Exception
     */
    public void updateNodeValue(String nodePath,String value) throws Exception{
        checkConnection();
        Stat status = new Stat();
        client.getData()
                .storingStatIn(status)
                .forPath(nodePath);
        int version = status.getVersion();
        // 版本号不一致会导致修改失败
        client.setData()
                .withVersion(version)
                .forPath(nodePath,value.getBytes());
    }


    /**
     * 删除指定路径的Zookeeper节点
     * @param nodePath 指定路径
     * @param if_guaranteed 是否保证删除
     *                      true: 删除节点时保证删除成功
     *                      false: 删除节点时不保证删除成功
     * @throws Exception
     */
    public void deleteNode(String nodePath,boolean if_guaranteed) throws Exception{
        checkConnection();
        if(if_guaranteed) {
            client.delete()
                    .guaranteed()
                    .deletingChildrenIfNeeded()
                    .forPath(nodePath);
        }else{
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(nodePath);
        }
    }

    /**
     * 检查指定路径的Zookeeper节点是否存在
     * @param nodePath 指定路径
     * @return 返回布尔值
     * @throws Exception
     */
    public boolean checkNodeExist(String nodePath) throws Exception {
        checkConnection();
        Stat s = client.checkExists().forPath(nodePath);
        return s != null;
    }


    /**
     * 监控指定路径的Zookeeper节点的子节点变化
     * @param nodePath 指定路径
     * @throws Exception
     */
    public void monitorChildrenNodes(String nodePath) throws Exception{
        // 创建一个PathChildrenCache对象，用于监控指定路径的子节点变化
        final PathChildrenCache cache = new PathChildrenCache(client,nodePath,true);
        // 添加监听器，当子节点发生变化时，触发监听器的事件
        cache.getListenable().addListener(new ChildrenListener(this,tableManager),pool);
        // 启动监听器
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
    }

}
