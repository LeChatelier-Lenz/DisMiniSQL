package MasterManagers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TableManagerTest {

    private TableManager tableManager;

    @BeforeAll
    void initAll() {
        // 全局初始化（可选）
    }

    @BeforeEach
    void setUp() throws IOException {
        tableManager = new TableManager();
        // 每个测试方法前重置状态（保持独立）
        tableManager.clearAll();
    }

    @Test
    @DisplayName("测试1: 添加服务器")
    void testAddServer() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.1"));
        assertTrue(tableManager.addServer("192.168.1.2"));
        assertFalse(tableManager.addServer("192.168.1.1")); // 重复添加

        assertEquals(2, tableManager.getIPListSize());
        assertTrue(tableManager.isExistServer("192.168.1.1"));
    }

    @Test
    @DisplayName("测试2: 添加表")
    void testAddTable() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.1"));
        assertTrue(tableManager.addTable("table1", "192.168.1.1"));
        assertTrue(tableManager.addTable("table2", "192.168.1.1"));

        assertEquals(2, tableManager.getTableList("192.168.1.1").size());

        assertTrue(tableManager.addTable("table1", "192.168.1.2"));
        assertEquals(2, tableManager.getRegionIPs("table1").size());
    }

    @Test
    @DisplayName("测试3: 删除表")
    void testDeleteTable() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.1"));
        assertTrue(tableManager.addTable("table1", "192.168.1.1"));
        assertTrue(tableManager.deleteTable("table1", "192.168.1.1"));

        assertNull(tableManager.getRegionIPs("table1"));
    }

    @Test
    @DisplayName("测试4: 服务器故障和恢复")
    void testServerFailureAndRecovery() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.2"));
        assertTrue(tableManager.addTable("table1", "192.168.1.2"));
        tableManager.transferTables("192.168.1.2", "192.168.1.1");

        List<String> tables = new ArrayList<>();
        tables.add("table1");
        assertTrue(tableManager.recoverServer("192.168.1.2", tables));
        assertEquals(1, tableManager.getTableList("192.168.1.2").size());
    }

    @Test
    @DisplayName("测试5: 表转移")
    void testTableTransfer() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.1"));
        assertTrue(tableManager.addServer("192.168.1.2"));
        assertTrue(tableManager.addTable("table4", "192.168.1.1"));
        assertTrue(tableManager.transferTables("192.168.1.1", "192.168.1.2"));

        assertTrue(tableManager.getTableList("192.168.1.2").contains("table4"));
    }

    @Test
    @DisplayName("测试6: 负载均衡")
    void testLoadBalancing() throws IOException {

        assertTrue(tableManager.addServer("192.168.1.1"));
        assertTrue(tableManager.addServer("192.168.1.2"));
        assertTrue(tableManager.addServer("192.168.1.3"));
        assertTrue(tableManager.addTable("table1", "192.168.1.1"));
        assertTrue(tableManager.addTable("table2", "192.168.1.2"));
        assertTrue(tableManager.addTable("table3", "192.168.1.2"));
        assertEquals("192.168.1.3", tableManager.getBestServer());
        assertEquals("192.168.1.1", tableManager.getBestServer("192.168.1.3"));
    }

    @AfterEach
    void tearDown() {
        // 每个测试方法后清理资源（可选）
    }

    @AfterAll
    void tearDownAll() {
        // 全局清理（可选）
    }
}


// 运行该测试 mvn compile test "-Dtest=MasterManagers.TableManagerTest"
