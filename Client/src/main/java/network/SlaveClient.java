package network;

import java.io.*;
import java.net.*;

public class SlaveClient {
    public String sendToSlave(String ip, int port, String sql) {
        try (Socket socket = new Socket(ip, port);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            out.println(sql);
            return in.readLine();
        } catch (IOException e) {
            return "Slave Error: " + e.getMessage();
        }
    }
}
