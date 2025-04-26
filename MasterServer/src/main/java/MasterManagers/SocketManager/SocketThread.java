package MasterManagers.SocketManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import MasterManagers.TableManager;
import lombok.extern.slf4j.Slf4j;

// 提供日志支持
@Slf4j
public class SocketThread implements Runnable {

    private boolean isRunning = false;
    private ClientProcessor clientProcessor;
    private RegionProcessor regionProcessor;

    public BufferedReader input = null;
    public PrintWriter output = null;

    public SocketThread(Socket _socket, TableManager _tableManager) throws IOException {
        this.clientProcessor = new ClientProcessor(_tableManager, _socket);
        this.regionProcessor = new RegionProcessor(_tableManager, _socket);
        this.isRunning = true;

        // 创建输入输出流
        this.input = new BufferedReader(new InputStreamReader(_socket.getInputStream()));
        this.output = new PrintWriter(_socket.getOutputStream(), true);
        System.out.println("服务端建立了新的客户端子线程:" + _socket.getInetAddress() + ":" + _socket.getPort());
        log.info("服务端建立了新的客户端子线程: {}:{}", _socket.getInetAddress(), _socket.getPort());
    }

    @Override
    public void run() {
        String cmd;
        try {
            while (isRunning) {
                Thread.sleep(1000);
                cmd = input.readLine();
                if (cmd != null) {
                    this.commandProcess(cmd);
                }
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }

    public void commandProcess(String cmd) {
        log.info(cmd);
        String result = "";
        // 命令分支，去掉前缀
        if (cmd.startsWith("<client>")) {

        } else if (cmd.startsWith("<region>")) {

        }
    }
}
