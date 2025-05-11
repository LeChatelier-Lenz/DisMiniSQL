import java.io.*;
import java.net.*;

public class test_MasterServer {
    private static final int PORT = 8888;

    public static void main(String[] args) {
        System.out.println("主节点模拟服务启动中... 端口：" + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                // 等待客户端连接
                Socket client = serverSocket.accept();
                // 处理客户端请求
                new Thread(() -> handleClient(client)).start();
            }
        } catch (IOException e) {
            System.err.println("主节点异常：" + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            // 读取客户端请求
            String request = in.readLine();
            if (request == null) return;

            System.out.println("接收到请求: " + request);

            if (request.startsWith("client[1]")) {
                // 查询表所在节点
                String table = request.substring(9).trim().toLowerCase();
                // 假设每个表名都被分配到一个从节点
                String response = "master[1]127.0.0.1:9999,127.0.0.1:9998"; // 假设返回两个从节点 IP
                out.println(response);

            } else if (request.startsWith("client[2]")) {
                // 创建表请求
                String table = request.substring(9).trim().toLowerCase();
                // 假设每个创建请求都分配到一个随机从节点
                String response = "master[2]127.0.0.1:9999"; // 假设返回一个从节点 IP
                out.println(response);

            } else if (request.startsWith("client[3]")) {
                // 删除表请求
                String table = request.substring(9).trim().toLowerCase();
                // 假设删除表的请求总是成功
                String response = "master[3]OK";
                out.println(response);

            } else {
                // 无法识别的请求
                out.println("UNKNOWN REQUEST");
            }
        } catch (IOException e) {
            System.err.println("处理客户端请求异常：" + e.getMessage());
        }
    }
}
