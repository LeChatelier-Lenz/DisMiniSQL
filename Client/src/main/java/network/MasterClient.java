package network;

import java.io.*;
import java.net.*;

public class MasterClient {
    private final String masterIP;
    private final int masterPort;

    public MasterClient(String masterIP, int masterPort) {
        this.masterIP = masterIP;
        this.masterPort = masterPort;
    }

    public String sendToMaster(String message) {
        try (Socket socket = new Socket(masterIP, masterPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(message);
            return in.readLine();  // 读一行响应
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}
