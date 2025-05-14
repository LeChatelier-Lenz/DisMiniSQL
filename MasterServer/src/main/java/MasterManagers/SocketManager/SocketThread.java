package MasterManagers.SocketManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

import MasterManagers.TableManager;

public class SocketThread implements Runnable {

    private volatile boolean isRunning = false;
    private final ClientProcessor clientProcessor;
    private final RegionProcessor regionProcessor;
    private final Socket socket;

    public BufferedReader input = null;
    public PrintWriter output = null;

    public SocketThread(Socket _socket, TableManager _tableManager) throws IOException {
        this.socket = _socket; // 保存Socket引用
        this.clientProcessor = new ClientProcessor(_tableManager, _socket);
        this.regionProcessor = new RegionProcessor(_tableManager, _socket);
        this.isRunning = true;

        // 创建输入输出流
        this.input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        this.output = new PrintWriter(_socket.getOutputStream(), true);
//        log.info("服务端建立了新的客户端子线程: {}:{}", _socket.getInetAddress(), _socket.getPort());
        System.out.println("主节点建立了新的socket进程 " + _socket.getInetAddress() + ":" + _socket.getPort());
    }

    @Override
    public void run() {
        String cmd;
        try {
            while (isRunning && !socket.isClosed()) { // 添加Socket状态检查
                cmd = input.readLine();
                if (cmd != null) {
                    commandProcess(cmd);
                }
            }
        } catch (SocketException e) {
//            log.debug("Socket连接已关闭: {}", e.getMessage());
            System.out.println("Socket连接已关闭: " + e.getMessage());
        } catch (IOException e) {
//            log.error("I/O错误: {}", e.getMessage());
            System.out.println("I/O错误: " + e.getMessage());
        } finally {
            close(); // 确保资源释放
        }
    }

    public boolean isClosed() {
        return socket == null || socket.isClosed();
    }

    public void close() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
//                log.info("Socket连接已关闭: {}:{}",
//                        socket.getInetAddress(), socket.getPort());
                System.out.println("Socket连接已关闭: " + socket.getInetAddress() + ":" + socket.getPort());
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
//            log.error("关闭连接时发生错误: {}", e.getMessage());
            System.out.println("关闭连接时发生错误: " + e.getMessage());
        }
    }

    public String commandProcess(String cmd) {
//        log.info(cmd);
        System.out.println("命令处理: " + cmd);
        String result = "";
        if (cmd.startsWith("<client>")) {
            result = this.clientProcessor.processClientCommand(cmd.substring(8));
        } else if (cmd.startsWith("<region>")) {
            result = this.regionProcessor.processRegionCommand(cmd.substring(8));
        }
        if (!result.equals("")) {
            sendCommand(result);
        }
        return "<master>" + result;
    }

    public void sendCommand(String cmd) {
        output.println("<master>" + cmd);
    }
}
