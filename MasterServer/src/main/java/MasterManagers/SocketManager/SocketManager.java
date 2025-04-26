package MasterManagers.SocketManager;

import MasterManagers.TableManager;

import java.io.IOException;

public class SocketManager {
    private int port;
    private TableManager tableManager;

    public SocketManager(int port, TableManager tableManager) {
        this.port = port;
        this.tableManager = tableManager;
    }

    public void startService() throws InterruptedException, IOException {

    }
}
