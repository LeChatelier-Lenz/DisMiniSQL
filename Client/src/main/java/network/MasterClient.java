package network;

import java.io.*;
import java.net.*;

public class MasterClient {
    private final String masterIP;
    private final int masterPort;
    private final int timeout; // 超时时间，单位为毫秒

    public MasterClient(String masterIP, int masterPort, int timeout) {
        this.masterIP = masterIP;
        this.masterPort = masterPort;
        this.timeout = timeout;
    }

    public String sendToMaster(String message) {
        try (Socket socket = new Socket(masterIP, masterPort)) {
            socket.setSoTimeout(timeout); // 设置超时时间
            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                out.println(message);
                return in.readLine();  // 读一行响应
            } catch (SocketTimeoutException e) {
                return "Error: Timeout occurred while waiting for server response.";
            }
        } catch (IOException e) {
            return "Error: " + e.getMessage();
        }
    }
}