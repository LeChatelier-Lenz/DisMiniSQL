package MasterManagers.ZookeeperManager;

import MasterManagers.TableManager;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

public class ChildrenListenerTest {

    @Mock
    private CuratorClient mockClient;
    
    @Mock
    private TableManager mockTableManager;
    
    @Mock
    private CuratorFramework mockCuratorFramework;
    
    @Mock
    private PathChildrenCacheEvent mockEvent;
    
    @Mock
    private PathChildrenCacheEvent.Data mockEventData;

    private ChildrenListener childrenListener;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        childrenListener = new ChildrenListener(mockClient, mockTableManager);
    }

    @Test
    public void testChildEvent_ChildAdded() throws Exception {
        // 准备测试数据
        String testPath = "/db/Region_1";
        String testValue = "localhost:2181";
        
        // 设置mock行为
        when(mockEvent.getType()).thenReturn(Type.CHILD_ADDED);
        when(mockEvent.getData()).thenReturn(mockEventData);
        when(mockEventData.getPath()).thenReturn(testPath);
        when(mockClient.getNodeValue(testPath)).thenReturn(testValue);

        // 执行测试
        childrenListener.childEvent(mockCuratorFramework, mockEvent);

        // 验证结果
        verify(mockClient).getNodeValue(testPath);
    }

    @Test
    public void testChildEvent_ChildRemoved() throws Exception {
        // 准备测试数据
        String testPath = "/db/Region_1";
        String testValue = "localhost:2181";
        
        // 设置mock行为
        when(mockEvent.getType()).thenReturn(Type.CHILD_REMOVED);
        when(mockEvent.getData()).thenReturn(mockEventData);
        when(mockEventData.getPath()).thenReturn(testPath);
        when(mockEventData.getData()).thenReturn(testValue.getBytes());

        // 执行测试
        childrenListener.childEvent(mockCuratorFramework, mockEvent);

        // 验证结果
        verify(mockEventData).getData();
    }

    @Test
    public void testChildEvent_ChildUpdated() throws Exception {
        // 准备测试数据
        String testPath = "/db/Region_1";
        String testValue = "localhost:2181";
        
        // 设置mock行为
        when(mockEvent.getType()).thenReturn(Type.CHILD_UPDATED);
        when(mockEvent.getData()).thenReturn(mockEventData);
        when(mockEventData.getPath()).thenReturn(testPath);
        when(mockClient.getNodeValue(testPath)).thenReturn(testValue);

        // 执行测试
        childrenListener.childEvent(mockCuratorFramework, mockEvent);

        // 验证结果
        verify(mockClient).getNodeValue(testPath);
    }

    @Test
    public void testEventServerAppear_NewServer() {
        // 准备测试数据
        String hostName = "Region_1";
        String hostUrl = "localhost:2181";
        
        // 设置mock行为
        when(mockClient.getNodeValue(anyString())).thenReturn(hostUrl);

        // 执行测试
        childrenListener.eventServerAppear(hostName, hostUrl);

        // 验证结果 - 这里需要根据实际实现来验证
        // 由于StrategyExecutor是内部创建的，我们需要通过其他方式来验证结果
    }

    @Test
    public void testEventServerDisappear() {
        // 准备测试数据
        String hostName = "Region_1";
        String hostUrl = "localhost:2181";

        // 执行测试
        childrenListener.eventServerDisappear(hostName, hostUrl);

        // 验证结果 - 这里需要根据实际实现来验证
        // 由于StrategyExecutor是内部创建的，我们需要通过其他方式来验证结果
    }
} 