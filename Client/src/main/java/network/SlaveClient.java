package network;

import java.io.*;
import java.net.*;
import utils.SQLParser;
import utils.SQLParser.SQLInfo;
import utils.SQLParser.SQLType;

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
            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                // SELECT：读取多行
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    if("END_OF_RESPONSE".equals(line)){
                        break;
                    }
                    response.append(line).append("\n");
                    //System.out.println("接受内容："+ line);
                }
                //System.out.println("接受结束");
                return response.toString().trim();
            } else if (sql.trim().toUpperCase().startsWith("CREATE")) {
                // SELECT：读取多行
                StringBuilder response = new StringBuilder();
                String line= in.readLine();
                SQLInfo info = SQLParser.parse(sql);
                if (line != null && line.contains("-->Create table") && line.contains(info.tableName + " successfully!")) {
                    return "success";
                } else {
                    return line;
                }
            }else if (sql.trim().toUpperCase().startsWith("INSERT")) {
                // SELECT：读取多行
                StringBuilder response = new StringBuilder();
                String line= in.readLine();
                if (line != null && line.contains("-->Insert successfully")) {
                    return "success";
                } else {
                    return line;
                }
            }else if (sql.trim().toUpperCase().startsWith("DELETE")) {
                // 删除操作，读取响应第一行并判断是否包含“-->Delete”以及数字大于零
                String line = in.readLine();
                if (line != null && line.contains("-->Delete")) {
                    // 提取数字部分
                    String[] parts = line.split(" ");
                    for (String part : parts) {
                        try {
                            int number = Integer.parseInt(part);
                            if (number > 0) {
                                return "success";
                            } else {
                                return "false";
                            }
                        } catch (NumberFormatException e) {
                            // 继续检查下一个部分
                        }
                    }
                }
                return "false";
            } else if (sql.trim().toUpperCase().startsWith("DROP")) {
                // SELECT：读取多行
                StringBuilder response = new StringBuilder();
                String line= in.readLine();
                SQLInfo info = SQLParser.parse(sql);
                if (line != null && line.contains("-->Drop table") && line.contains(info.tableName + " successfully!")) {
                    return "success";
                } else {
                    return line;
                }
            }else{
                // 非 SELECT：只读取一行
                return in.readLine();
            }



        } catch (IOException e) {
            // 6. 如果出现异常，返回错误信息
            return "Slave Error: " + e.getMessage();
        }
    }
}
