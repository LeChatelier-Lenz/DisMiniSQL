package MasterManagers.SocketManager;

import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import MasterManagers.TableManager;

@ExtendWith(MockitoExtension.class)
class ClientProcessorTest {

    @Mock
    private TableManager tableManager;

    @Mock
    private Socket socket;

    private ClientProcessor clientProcessor;

    @BeforeEach
    void setUp() {
        clientProcessor = new ClientProcessor(tableManager, socket);
    }

    @Test
    void testProcessClientCommand_QueryTable() {
        // 模拟TableManager返回
        when(tableManager.getRegionIPs("testTable")).thenReturn(List.of("192.168.1.1"));

        // 执行测试
        String result = clientProcessor.processClientCommand("[1]testTable");

        // 验证结果
        assertEquals("[1]192.168.1.1", result);
        verify(tableManager).getRegionIPs("testTable");
    }

    @Test
    void testProcessClientCommand_CreateTable() {
        // 模拟负载均衡选择
        when(tableManager.getBestServer()).thenReturn("192.168.1.2");

        String result = clientProcessor.processClientCommand("[2]newTable");

        assertEquals("[2]192.168.1.2", result);
        verify(tableManager).addTable("newTable", "192.168.1.2");
    }

    @Test
    void testProcessClientCommand_DeleteTable() {
        when(tableManager.deleteTableFromAllServers("toDelete")).thenReturn(true);

        String result = clientProcessor.processClientCommand("[3]toDelete");

        assertEquals("[3]OK", result);
        verify(tableManager).deleteTableFromAllServers("toDelete");
    }

    @Test
    void testProcessClientCommand_InvalidCommand() {
        String result = clientProcessor.processClientCommand("[4]invalid");
        assertTrue(result.isEmpty());
    }
}

// 运行socket相关所有测试 mvn test "-Dtest=MasterManagers.SocketManager.*Test"
