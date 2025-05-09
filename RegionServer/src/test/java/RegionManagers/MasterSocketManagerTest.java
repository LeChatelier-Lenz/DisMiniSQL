package RegionManagers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;

public class  MasterSocketManagerTest {
    private static final int MASTER_PORT = 12345;
    private static CountDownLatch latch = new CountDownLatch(3); // 三个测试指令

    public static void main(String[] args) throws Exception {
        System.out.println("测试：连接主节点服务端...");
        try (Socket socket = new Socket("localhost", MASTER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // 启动监听线程读取主节点发来的指令
            new Thread(() -> {
                try {
                    String line;
                    while ((line = in.readLine()) != null) {
                        System.out.println("测试：收到主节点指令 => " + line);
                        if (line.contains("[3]")) {
                            System.out.println("✓ 收到容灾恢复指令");
                            latch.countDown();
                        } else if (line.contains("[4]")) {
                            System.out.println("✓ 收到节点初始化指令");
                            latch.countDown();
                        } else if (line.contains("[5]")) {
                            System.out.println("✓ 收到负载均衡迁移指令");
                            latch.countDown();
                        }
                    }
                } catch (IOException e) {
                    System.err.println("读取失败：" + e.getMessage());
                }
            }).start();

            // 等待所有指令接收完毕
            latch.await();
            System.out.println("测试完成：所有主节点指令已成功接收");

        } catch (IOException e) {
            System.err.println("连接主节点失败：" + e.getMessage());
        }
    }
}
