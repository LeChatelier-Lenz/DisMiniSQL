package network;

import java.io.*;
import java.net.*;

public class SlaveClient {

    /**
     * 向某个从节点（Slave）发送 SQL 查询，并接收响应结果
     *
     * @param ip  从节点的 IP 地址
     * @param port 从节点监听的端口号
     * @param sql 要执行的 SQL 查询语句（如 "SELECT * FROM users;"）
     * @return 从节点返回的结果，或错误信息
     */
    public String sendToSlave(String ip, int port, String sql) {
        try (
                // 1. 创建与从节点的 Socket 连接
                Socket socket = new Socket(ip, port);

                // 2. 获取输出流，向从节点发送 SQL 指令
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);  // true 表示自动 flush

                // 3. 获取输入流，读取从节点的响应
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
        ) {
            // 4. 向从节点发送 SQL 语句
            out.println(sql);

            // 5. 接收并返回从节点的第一行响应结果（以换行符结束）
            return in.readLine();

        } catch (IOException e) {
            // 6. 如果出现异常，返回错误信息
            return "Slave Error: " + e.getMessage();
        }
    }
}
