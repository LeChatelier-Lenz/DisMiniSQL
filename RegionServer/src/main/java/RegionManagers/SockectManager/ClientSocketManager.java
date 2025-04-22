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
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                Client client = new Client(socket, masterSocketManager);
                // 启动客户端连接
                Thread thread = new Thread(client);
                // 把socket和子线程放入hashmap中
                clientHashMap.put(socket, thread);
                thread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
