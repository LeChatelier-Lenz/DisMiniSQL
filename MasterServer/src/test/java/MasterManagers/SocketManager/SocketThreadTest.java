package MasterManagers.SocketManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import MasterManagers.TableManager;

@ExtendWith(MockitoExtension.class)
class SocketThreadTest {

    @Mock
    private TableManager tableManager;

    @Mock
    private Socket socket;

    @Mock
    private BufferedReader input;

    @Mock
    private PrintWriter output;

    private SocketThread socketThread;

    @BeforeEach
    void setUp() throws Exception {
        when(socket.getInputStream()).thenReturn(new java.io.ByteArrayInputStream("test".getBytes()));
        when(socket.getOutputStream()).thenReturn(new java.io.ByteArrayOutputStream());

        socketThread = new SocketThread(socket, tableManager);
        setFinalField(socketThread, "input", input);
        setFinalField(socketThread, "output", output);
    }

    @Test
    void testCommandProcess() throws IOException {
        // 1. 准备测试数据（模拟TableManager行为）
        String testTableName = "testTable";
        List<String> mockIPs = List.of("192.168.1.1", "192.168.1.2");

        // 显式模拟getRegionIPs的行为
        when(tableManager.getRegionIPs(testTableName)).thenReturn(mockIPs);

        // 2. 执行测试
        String result = socketThread.commandProcess("<client>[1]" + testTableName);

        // 3. 验证结果
        // 验证是否调用了tableManager的方法
        verify(tableManager).getRegionIPs(testTableName);

        // 验证返回格式（注意原逻辑会包装<master>前缀）
        assertEquals("<master>[1]192.168.1.1,192.168.1.2", result);
    }

    @Test
    void testClose() throws IOException {
        // 配置Socket的isClosed()行为
        when(socket.isClosed()).thenReturn(false); // 初始状态
        doAnswer(invocation -> {
            // 标记socket已关闭
            when(socket.isClosed()).thenReturn(true);
            return null;
        }).when(socket).close();

        // 执行测试
        socketThread.close();

        // 验证状态
        assertTrue(socketThread.isClosed());
        verify(socket).close();
        verify(input).close();
        verify(output).close();
    }

    private void setFinalField(Object target, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        // 移除对modifiers字段的操作
        field.set(target, value);
    }
}

// 运行socket相关所有测试 mvn test "-Dtest=MasterManagers.SocketManager.*Test"
