package MasterManagers.ZookeeperManager;

import MasterManagers.TableManager;
//import MasterManagers.ZookeeperManager.CuratorClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.*;

/**
 * ZooKeeper的节点监视器，将发生的事件进行处理
 */
public class ChildrenListener implements PathChildrenCacheListener {

    private CuratorClient client;
    private TableManager tableManger;

    public ChildrenListener(CuratorClient curatorClient, TableManager tableManger) {
        this.tableManger = tableManger;
//        this.strategyExecutor = new ServiceStrategyExecutor(tableManger);
        this.client = curatorClient;
    }

    @Override
    public void childEvent(CuratorFramework client,PathChildrenCacheEvent event) throws Exception{
    }
}
