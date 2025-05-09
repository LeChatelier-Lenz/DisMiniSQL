package MasterManagers.ZookeeperManager;

import MasterManagers.SocketManager.SocketThread;
import MasterManagers.TableManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StrategyExecutorTest {

    @Mock
    private TableManager mockTableManager;

    @Mock
    private SocketThread mockSocketThread;

    private StrategyExecutor strategyExecutor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        strategyExecutor = new StrategyExecutor(mockTableManager);
    }

    @Test
    public void testExistServer() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        when(mockTableManager.isExistServer(hostUrl)).thenReturn(true);

        // 执行测试
        boolean exists = strategyExecutor.existServer(hostUrl);

        // 验证结果
        assertTrue(exists);
        verify(mockTableManager).isExistServer(hostUrl);
    }

    @Test
    public void testExecStrategy_Recover() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        when(mockTableManager.getSocketThread(hostUrl)).thenReturn(mockSocketThread);

        // 执行测试
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.RECOVER);

        // 验证结果
        verify(mockTableManager).recoverServer(eq(hostUrl), any(List.class));
        verify(mockTableManager).getSocketThread(hostUrl);
        verify(mockSocketThread).sendCommand("[4]recover");
    }

    @Test
    public void testExecStrategy_Invalid() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        String bestInet = "localhost:2182";
        List<String> tableList = Arrays.asList("table1", "table2");
        
        when(mockTableManager.getTableList(hostUrl)).thenReturn(tableList);
        when(mockTableManager.getBestServer(hostUrl)).thenReturn(bestInet);
        when(mockTableManager.getSocketThread(bestInet)).thenReturn(mockSocketThread);

        // 执行测试
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.INVALID);

        // 验证结果
        verify(mockTableManager).getTableList(hostUrl);
        verify(mockTableManager).getBestServer(hostUrl);
        verify(mockTableManager).transferTables(hostUrl, bestInet);
        verify(mockTableManager).getSocketThread(bestInet);
        verify(mockSocketThread).sendCommand(anyString());
    }

    @Test
    public void testExecStrategy_Discover() {
        // 准备测试数据
        String hostUrl = "localhost:2181";

        // 执行测试
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.DISCOVER);

        // 验证结果 - 由于execDiscoverStrategy是空实现，这里只验证没有异常抛出
    }

    @Test
    public void testExecStrategy_WithException() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        when(mockTableManager.getSocketThread(hostUrl)).thenThrow(new RuntimeException("Test exception"));

        // 执行测试 - 应该捕获异常并记录日志，但不抛出异常
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.RECOVER);

        // 验证结果
        verify(mockTableManager).getSocketThread(hostUrl);
        // 验证异常被正确处理，没有抛出到测试方法
    }

    @Test
    public void testExecInvalidStrategy_TableTransfer() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        String bestInet = "localhost:2182";
        List<String> tableList = new ArrayList<>();
        tableList.add("table1");
        tableList.add("table2");

        when(mockTableManager.getTableList(hostUrl)).thenReturn(tableList);
        when(mockTableManager.getBestServer(hostUrl)).thenReturn(bestInet);
        when(mockTableManager.getSocketThread(bestInet)).thenReturn(mockSocketThread);

        // 执行测试
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.INVALID);

        // 验证结果
        verify(mockTableManager).transferTables(hostUrl, bestInet);
        verify(mockSocketThread).sendCommand(contains("[3]" + hostUrl + "#table1table2"));
    }

    @Test
    public void testExecRecoverStrategy_EmptyTableList() {
        // 准备测试数据
        String hostUrl = "localhost:2181";
        when(mockTableManager.getSocketThread(hostUrl)).thenReturn(mockSocketThread);

        // 执行测试
        strategyExecutor.execStrategy(hostUrl, StrategyTypeEnum.RECOVER);

        // 验证结果
        verify(mockTableManager).recoverServer(eq(hostUrl), any(List.class));
        verify(mockSocketThread).sendCommand("[4]recover");
    }
} 