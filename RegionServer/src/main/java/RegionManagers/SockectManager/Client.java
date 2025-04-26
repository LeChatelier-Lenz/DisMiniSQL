package RegionManagers.SockectManager;

import miniSQL.API;
import miniSQL.Interpreter;

import java.io.*;
import java.net.Socket;

public class Client  implements Runnable{
    private Socket socket;
    // 客户端输入
    private BufferedReader input = null;
    private BufferedWriter bufferedWriter = null;
    // 处理后输出
    private PrintWriter output = null;
    private PrintStream printStream = null;  //暂时未用到
    private Boolean isRunning = false;
    private MasterSocketManager masterSocketManager = null;

    public Client(Socket socket, MasterSocketManager masterSocketManager) throws IOException {
        this.socket = socket;
        this.input = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        this.output = new PrintWriter(this.socket.getOutputStream(), true);
        this.printStream = new PrintStream(this.socket.getOutputStream());
        this.masterSocketManager = masterSocketManager;
        this.isRunning = true;
    }

    @Override
    public void run() {
        String command = null;
        try {
            while (isRunning) {
                // 查看socket状态
                if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                    isRunning = false;
                    break;
                }
                Thread.sleep(1000);
                // 读取客户端输入命令
                command = input.readLine();
                if (command != null) {
                    System.out.println("REGION> 客户端：" + socket.getInetAddress() + socket.getPort() + "\n   指令：" + command);
                    // 处理传入命令
                    String res = commandExcutor(command, socket.getInetAddress().toString());
                    // 从节点有表更改，发送给主节点
                    if(!res.equals("No Table Modified")) {
                        masterSocketManager.sendToMaster(res);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String commandExcutor(String command, String ip) throws Exception {
        String[] sqlParts = command.trim().split("\s+");
        String operation = sqlParts[0].toUpperCase();
        // 执行结果
        String response = Interpreter.interpret(command);
        API.store();
        String[] responseParts = response.trim().split("\s+");
        // 发送响应给客户端
        output.println(response);

        switch (operation) {
            case "SELECT":
                return "No Table Modified";

            case "CREATE":
                if (responseParts.length < 3) {
                    return "error";
                }
                // 更新数据并生成同步信息
                return "<region>[]2 " + responseParts[2] + " create";

            case "DROP":
                if (responseParts.length < 3) {
                    return "error";
                }
                return "<region>[]2 " + responseParts[2] + " drop";

            case "INSERT":
                if (sqlParts.length < 3) {
                    return "error";
                }
                return "No Table Modified";

            case "UPDATE":
                if (sqlParts.length < 3) {
                    return "error";
                }
                return "No Table Modified";

            case "DELETE":
                if (sqlParts.length < 3) {
                    return "error";
                }
                return "No Table Modified";

            default:
                return "***ERROR*** : Unknown operation";
        }

    }
}
