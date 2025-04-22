package RegionManagers.SockectManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class ClientSocketManager implements Runnable {
    private ServerSocket serverSocket;
    private MasterSocketManager masterSocketManager;
    private HashMap<Socket, Thread> clientHashMap;

    public ClientSocketManager(int port, MasterSocketManager masterSocketManager)
            throws IOException, InterruptedException {
        this.serverSocket = new ServerSocket(port);
        this.masterSocketManager = masterSocketManager;
        this.clientHashMap = new HashMap<Socket, Thread>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000);
                // 等待与之连接的客户端
                Socket socket = serverSocket.accept();
                // 建立子线程并启动
                Client client = new Client(socket, masterSocketManager);
                Thread thread = new Thread(client);
                // 把子线程放入hashmap中
                clientHashMap.put(socket, thread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
