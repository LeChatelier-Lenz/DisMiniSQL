package MasterManagers.ZookeeperManager;

import MasterManagers.TableManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CuratorClientTest {

    @Mock
    private CuratorFramework mockCuratorFramework;

    @Mock
    private TableManager mockTableManager;

    @Mock
    private Stat mockStat;

    private CuratorClient curatorClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        curatorClient = new CuratorClient(mockTableManager);
        // 使用反射设置mock的CuratorFramework
        try {
            java.lang.reflect.Field field = CuratorClient.class.getDeclaredField("client");
            field.setAccessible(true);
            field.set(curatorClient, mockCuratorFramework);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
    }

    @Test
    public void testCreateNode() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        String value = "testValue";

        // 执行测试
        curatorClient.createNode(nodePath, value);

        // 验证结果
        verify(mockCuratorFramework).create()
                .creatingParentsIfNeeded()
                .forPath(eq(nodePath), eq(value.getBytes()));
    }

    @Test
    public void testCreateNodeWithMode() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        String value = "testValue";
        CreateMode mode = CreateMode.PERSISTENT;

        // 执行测试
        curatorClient.createNode(nodePath, value, mode);

        // 验证结果
        verify(mockCuratorFramework).create()
                .creatingParentsIfNeeded()
                .withMode(eq(mode))
                .forPath(eq(nodePath), eq(value.getBytes()));
    }

    @Test
    public void testGetNodeValue() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        String expectedValue = "testValue";
        when(mockCuratorFramework.getData().forPath(nodePath))
                .thenReturn(expectedValue.getBytes());

        // 执行测试
        String actualValue = curatorClient.getNodeValue(nodePath);

        // 验证结果
        assertEquals(expectedValue, actualValue);
        verify(mockCuratorFramework).getData().forPath(nodePath);
    }

    @Test
    public void testGetNodeChildren() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        List<String> expectedChildren = Arrays.asList("child1", "child2");
        when(mockCuratorFramework.getChildren().forPath(nodePath))
                .thenReturn(expectedChildren);

        // 执行测试
        List<String> actualChildren = curatorClient.getNodeChildren(nodePath);

        // 验证结果
        assertEquals(expectedChildren, actualChildren);
        verify(mockCuratorFramework).getChildren().forPath(nodePath);
    }

    @Test
    public void testUpdateNodeValue() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        String newValue = "newValue";
        when(mockCuratorFramework.getData().storingStatIn(any(Stat.class)).forPath(nodePath))
                .thenReturn("oldValue".getBytes());

        // 执行测试
        curatorClient.updateNodeValue(nodePath, newValue);

        // 验证结果
        verify(mockCuratorFramework).setData()
                .withVersion(anyInt())
                .forPath(eq(nodePath), eq(newValue.getBytes()));
    }

    @Test
    public void testDeleteNode() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";

        // 执行测试 - 测试非保证删除
        curatorClient.deleteNode(nodePath, false);

        // 验证结果
        verify(mockCuratorFramework).delete()
                .deletingChildrenIfNeeded()
                .forPath(nodePath);

        // 执行测试 - 测试保证删除
        curatorClient.deleteNode(nodePath, true);

        // 验证结果
        verify(mockCuratorFramework).delete()
                .guaranteed()
                .deletingChildrenIfNeeded()
                .forPath(nodePath);
    }

    @Test
    public void testCheckNodeExist() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        when(mockCuratorFramework.checkExists().forPath(nodePath))
                .thenReturn(mockStat);

        // 执行测试
        boolean exists = curatorClient.checkNodeExist(nodePath);

        // 验证结果
        assertTrue(exists);
        verify(mockCuratorFramework).checkExists().forPath(nodePath);
    }

    @Test
    public void testMonitorChildrenNodes() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";

        // 执行测试
        curatorClient.monitorChildrenNodes(nodePath);

        // 验证结果 - 由于PathChildrenCache是内部创建的，我们只能验证基本行为
        // 这里可能需要更复杂的测试设置来验证监听器的添加
    }

    @Test(expected = RuntimeException.class)
    public void testCreateNodeWithInvalidMode() throws Exception {
        // 准备测试数据
        String nodePath = "/test/node";
        String value = "testValue";
        CreateMode invalidMode = null;

        // 执行测试 - 应该抛出异常
        curatorClient.createNode(nodePath, value, invalidMode);
    }
} 