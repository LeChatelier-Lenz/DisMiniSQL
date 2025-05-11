package RegionManagers;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MasterSimulator {
    public static final int PORT = 12345;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("主节点模拟器启动，等待从节点连接...");

        Socket client = serverSocket.accept();
        System.out.println("从节点已连接：" + client.getInetAddress());

        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        PrintWriter output = new PrintWriter(client.getOutputStream(), true);

        // 接收从节点数据（可选）
        new Thread(() -> {
            String line;
            try {
                while ((line = input.readLine()) != null) {
                    System.out.println("收到从节点：" + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // 控制台发送测试指令
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("请输入指令发送给从节点：");
        System.out.println("1 - 发送容灾恢复指令");
        System.out.println("2 - 发送节点初始化指令");
        System.out.println("3 - 发送负载均衡迁移指令");
        System.out.println("其他键退出");

        while (true) {
            String cmd = console.readLine();
            if (cmd.equals("1")) {
                output.println("<master>[3]testTable1@create table testTable1(id int);#testTable2@create table testTable2(name char(20));");
            } else if (cmd.equals("2")) {
                output.println("<master>[4]recover");
            } else if (cmd.equals("3")) {
                output.println("<master>[5]127.0.0.1 testTable1");
            } else {
                break;
            }
        }

        client.close();
        serverSocket.close();
        System.out.println("主节点模拟器关闭。");
    }
}
