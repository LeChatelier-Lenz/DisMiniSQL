package MasterManagers.SocketManager;

import java.net.InetAddress;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import MasterManagers.TableManager;
import MasterManagers.Utils.SocketUtils;

@ExtendWith(MockitoExtension.class)
class RegionProcessorTest {

    @Mock
    private TableManager tableManager;

    @Mock
    private Socket socket;

    private RegionProcessor regionProcessor;

    @BeforeEach
    void setUp() throws Exception {
        // 1. 创建InetAddress的mock对象
        InetAddress mockedAddress = mock(InetAddress.class);
        when(mockedAddress.getHostAddress()).thenReturn("127.0.0.1");

        // 2. 模拟Socket行为
        when(socket.getInetAddress()).thenReturn(mockedAddress);

        // 3. 模拟静态方法
        try (var mockedStatic = mockStatic(SocketUtils.class)) {
            mockedStatic.when(SocketUtils::getHostAddress).thenReturn("192.168.1.2");

            // 4. 初始化被测对象
            regionProcessor = new RegionProcessor(tableManager, socket);
        }
    }

    @Test
    void testProcessRegionCommand_ReportTables() {
        try (var mockedStatic = mockStatic(SocketUtils.class)) {
            mockedStatic.when(SocketUtils::getHostAddress).thenReturn("192.168.1.2");
            regionProcessor = new RegionProcessor(tableManager, socket);

            String result = regionProcessor.processRegionCommand("[1]table1 table2");

            verify(tableManager).addTables(List.of("table1", "table2"), "192.168.1.2");
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testProcessRegionCommand_AddTable() {
        try (var mockedStatic = mockStatic(SocketUtils.class)) {
            mockedStatic.when(SocketUtils::getHostAddress).thenReturn("192.168.1.2");
            regionProcessor = new RegionProcessor(tableManager, socket);

            String result = regionProcessor.processRegionCommand("[2]newTable ADD");

            verify(tableManager).addTable("newTable", "192.168.1.2");
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testProcessRegionCommand_DisasterRecovery() {
        try (var mockedStatic = mockStatic(SocketUtils.class)) {
            mockedStatic.when(SocketUtils::getHostAddress).thenReturn("192.168.1.2");
            regionProcessor = new RegionProcessor(tableManager, socket);

            String result = regionProcessor.processRegionCommand("[3]Complete disaster recovery");

            verify(tableManager).recoverServer("192.168.1.2", List.of());
            assertTrue(result.isEmpty());
        }
    }

    @Test
    void testProcessRegionCommand_ClearData() {
        String result = regionProcessor.processRegionCommand("[4]");

        assertTrue(result.isEmpty());
    }
}

// 运行socket相关所有测试 mvn test "-Dtest=MasterManagers.SocketManager.*Test"
