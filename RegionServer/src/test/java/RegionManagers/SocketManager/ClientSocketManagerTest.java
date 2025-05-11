package RegionManagers.SocketManager;

import RegionManagers.SockectManager.ClientSocketManager;
import RegionManagers.SockectManager.MasterSocketManager;
import miniSQL.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class ClientSocketManagerTest {
    private static final int TEST_PORT = 9999;
    private static volatile boolean isServerRunning = true; // 服务端运行标志
    private static CountDownLatch latch = new CountDownLatch(6); // 5个测试步骤

    public static void main(String[] args) throws Exception {
        MockMasterSocketManager mockMaster = new MockMasterSocketManager();
        API.initial();

        // 启动服务端
        Thread serverThread = new Thread(() -> {
            try {
                ClientSocketManager server = new ClientSocketManager(TEST_PORT, mockMaster);
                System.out.println("测试：服务端启动，监听端口 " + TEST_PORT);
                server.run();
                System.out.println("测试：服务端已关闭");
            } catch (IOException e) {
                System.err.println("服务端异常: " + e.getMessage());
            }
        });
        serverThread.start();

        Thread.sleep(1000); // 等待服务端启动

        // 启动客户端测试（含前置清理）
        new Thread(() -> {
            try (Socket clientSocket = new Socket("localhost", TEST_PORT);
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

//                // 前置清理：删除已存在的表
//                String dropPreCmd = "drop table if exists test_table;";
//                out.println(dropPreCmd);
//                String dropPreResponse = in.readLine();
//                System.out.println("测试：前置DROP响应: " + dropPreResponse);
//                latch.countDown();

                // 测试用例1：CREATE TABLE
                String createCmd = "create table test_table (id int, name char(10), primary key(id));";
                out.println(createCmd);
                String createResponse = in.readLine();
                System.out.println("测试：CREATE 响应: " + createResponse);
                latch.countDown();

                // 测试用例2：INSERT
                String insertCmd = "insert into test_table values (1, 'Alice');";
                out.println(insertCmd);
                String insertResponse = in.readLine();
                System.out.println("测试：INSERT 响应: " + insertResponse);
                latch.countDown();

                insertCmd = "insert into test_table values (2, 'Bob');";
                out.println(insertCmd);
                insertResponse = in.readLine();
                System.out.println("测试：INSERT 响应: " + insertResponse);
                latch.countDown();

                // 测试用例3：SELECT（读取完整响应）
                String selectCmd = "select * from test_table;";
                out.println(selectCmd);
                String selectResponse = readFullResponse(in);
                System.out.println("测试：SELECT 响应: " + selectResponse);
                latch.countDown();

                // 测试用例4：DROP TABLE
                String dropCmd = "drop table test_table;";
                out.println(dropCmd);
                String dropResponse = in.readLine();
                System.out.println("测试：DROP 响应: " + dropResponse);
                latch.countDown();

            } catch (IOException e) {
                System.err.println("客户端连接失败: " + e.getMessage());
            } finally {
                isServerRunning = false; // 触发服务端关闭
            }
        }).start();

        latch.await(); // 等待所有测试完成

        // 验证主节点同步消息
        System.out.println("\n测试验证结果：");
        System.out.println("主节点收到的最后同步消息: " + mockMaster.getLastSyncMessage());
    }

    // 读取完整响应（处理多行）
    private static String readFullResponse(BufferedReader in) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null) {
            response.append(line).append("\n");
            if (line.contains("-->Query ok!")) { // 假设响应以该字符串结尾
                break;
            }
        }
        return response.toString().trim();
    }

    // 模拟主节点（线程安全）
    static class MockMasterSocketManager extends MasterSocketManager {
        private volatile String lastSyncMessage;

        public MockMasterSocketManager() throws IOException {
        }

        @Override
        public void sendToMaster(String message) {
            this.lastSyncMessage = message;
            System.out.println("模拟主节点收到同步消息: " + message);
        }

        public String getLastSyncMessage() {
            return lastSyncMessage;
        }
    }
}