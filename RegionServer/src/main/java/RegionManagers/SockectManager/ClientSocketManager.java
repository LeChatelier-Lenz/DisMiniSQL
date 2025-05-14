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
            throws IOException {
        this.serverSocket = new ServerSocket(port);
//        this.serverSocket = new ServerSocket(0);   // 0 表示随机
//        System.out.println("Listening on port " + serverSocket.getLocalPort());
        this.masterSocketManager = masterSocketManager;
        this.clientHashMap = new HashMap<Socket, Thread>();
    }

    @Override
    public void run() {
        try {
            while (true) {
                Thread.sleep(1000);
                System.out.println("REGION> 等待客户端连接...");
                // 等待客户端连接
                Socket socket = serverSocket.accept();
                Client client = new Client(socket, masterSocketManager);
                // 启动客户端连接
                Thread thread = new Thread(client);
                // 把socket和子线程放入hashmap中
                clientHashMap.put(socket, thread);
                thread.start();
                System.out.println("REGION> 从节点线程已启动!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
