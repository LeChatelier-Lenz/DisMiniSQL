import java.io.*;
import java.net.*;

public class test_SlaveServer {
    private static final int PORT = 9999; // 从节点监听的端口号

    public static void main(String[] args) {
        System.out.println("从节点模拟服务启动中... 监听端口：" + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(() -> handleClient(client)).start();
            }
        } catch (IOException e) {
            System.err.println("从节点异常：" + e.getMessage());
        }
    }

    private static void handleClient(Socket socket) {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String sql = in.readLine();
            if (sql == null) return;

            System.out.println("收到客户端指令： " + sql); // 输出客户端 SQL

            // 构建响应：先回显 SQL，再返回模拟结果
            StringBuilder result = new StringBuilder();

            if (sql.toUpperCase().startsWith("SELECT")) {
                result.append("模拟查询结果：\nID | Name\n1  | Alice\n2  | Bob");
            } else if (sql.toUpperCase().startsWith("INSERT")) {
                result.append("模拟插入成功：影响 1 行。");
            } else if (sql.toUpperCase().startsWith("UPDATE")) {
                result.append("模拟更新成功：影响 1 行。");
            } else if (sql.toUpperCase().startsWith("DELETE")) {
                result.append("模拟删除成功：影响 1 行。");
            } else if (sql.toUpperCase().startsWith("CREATE")) {
                result.append("模拟建表成功。");
            } else if (sql.toUpperCase().startsWith("DROP")) {
                result.append("模拟删表成功。");
            } else {
                result.append("无法识别 SQL 类型。");
            }

            out.println(result.toString());

        } catch (IOException e) {
            System.err.println("处理客户端请求失败：" + e.getMessage());
        }
    }
}
