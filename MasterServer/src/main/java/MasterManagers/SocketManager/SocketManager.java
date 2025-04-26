package MasterManagers.SocketManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import MasterManagers.TableManager;

public class SocketManager {

    private ServerSocket serverSocket;
    private TableManager tableManager;

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
        }
    }
}
