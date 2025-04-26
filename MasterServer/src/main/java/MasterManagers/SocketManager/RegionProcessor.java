package MasterManagers.SocketManager;

import java.net.Socket;

import MasterManagers.TableManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RegionProcessor {

    private TableManager tableManger;
    private Socket socket;

    public RegionProcessor(TableManager _tableManger, Socket _socket) {
        this.tableManger = _tableManger;
        this.socket = _socket;
    }

    public String processRegionCommand(String cmd) {
        String result = "";

        return result;
    }
}
