package MasterManagers.SocketManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import MasterManagers.TableManager;
import MasterManagers.Utils.SocketUtils;

public class SocketManager {

    private final ServerSocket serverSocket;
    private final TableManager tableManager;

    public SocketManager(int port, TableManager tableManager) throws IOException, InterruptedException {
        this.tableManager = tableManager;
        this.serverSocket = new ServerSocket(port);
    }

    public void startService() throws InterruptedException, IOException {
        while (true) {
            // 等待连接的客户端
            Thread.sleep(200);

            Socket socket = serverSocket.accept();

            // 建立子线程并启动
            SocketThread socketThread = new SocketThread(socket, this.tableManager);
            String IP = socket.getInetAddress().getHostAddress();
            if (IP.equals("127.0.0.1")) {
                IP = SocketUtils.getHostAddress();
            }
            this.tableManager.addSocketThread(IP, socketThread);
            Thread thread = new Thread(socketThread);
            thread.start();
        }
    }
}
