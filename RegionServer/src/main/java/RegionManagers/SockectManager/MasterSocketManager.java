package RegionManagers.SockectManager;

import java.io.*;
import java.net.Socket;

import miniSQL.API;
import miniSQL.Interpreter;
import RegionManagers.DataBaseManager;

public class MasterSocketManager implements Runnable {
    private Socket socket;
    private BufferedReader input = null;
    private PrintWriter output = null;
    private FtpUtils ftpUtils;
    private DataBaseManager dataBaseManager;
    private boolean isRunning = false;

    public final int SERVER_PORT = 12345;
    public final String MASTER = "localhost";

    public MasterSocketManager() throws IOException {
        //this.socket = new Socket(MASTER, SERVER_PORT);
        this.ftpUtils = new FtpUtils();
        this.dataBaseManager = new DataBaseManager();
        //input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        //output = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;
    }

    public void sendToMaster(String modified_info) {
        output.println(modified_info);
    }

    public void sendTableInfoToMaster(String table_info) {
        output.println("<region>[1]" + table_info);
    }
    public void sendChangeNotification(String tableName, String action) {
        output.println("<region>[2]" + tableName + " " + action);
    }
    public void receiveFromMaster() throws IOException {
        String line = null;
        if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
            System.out.println("新消息>>>Socket已经关闭!");
            return;
        }

        line = input.readLine();
        if (line != null) {
            handleMasterCommand(line);
        }
    }

    public void handleMasterCommand(String line) {
        try {
            if (line.startsWith("<master>[3]")) {
                // 容灾恢复格式: <master>[3]表名1@SQL1#表名2@SQL2...
                String info = line.substring(11);
                if (info.isEmpty()) return;

                String[] tableSqlPairs = info.split("#");
                for (String pair : tableSqlPairs) {
                    String[] parts = pair.split("@", 2);
                    if (parts.length == 2) {
                        String table = parts[0];
                        String sql = parts[1];

                        delFile(table);
                        delFile(table + "_index.index");
                        ftpUtils.downLoadFile("table", table, "");
                        ftpUtils.downLoadFile("index", table + "_index.index", "");
                        Interpreter.interpret(sql);
                    }
                }

                ftpUtils.additionalDownloadFile("catalog", "table_catalog");
                ftpUtils.additionalDownloadFile("catalog", "index_catalog");

                API.initial();
                System.out.println("Disaster recovery complete.");
                output.println("<region>[3]Complete disaster recovery");

            } else if (line.equals("<master>[4]recover")) {
                // 节点初始化
                String tableName = dataBaseManager.getMetaInfo();
                String[] tableNames = tableName.split(" ");
                for (String table : tableNames) {
                    Interpreter.interpret("drop table " + table + " ;");
                }
                API.store();
                API.initial();
                output.println("<region>[4]");

            } else if (line.startsWith("<master>[5]")) {
                // 负载均衡迁移：<master>[5]目标IP 表名
                String payload = line.substring(11).trim();
                String[] parts = payload.split(" ");
                if (parts.length == 2) {
                    String targetIp = parts[0];
                    String tableName = parts[1];

                    ftpUtils.uploadFile("table", tableName, targetIp);
                    ftpUtils.uploadFile("index", tableName + "_index.index", targetIp);
                    ftpUtils.uploadFile("catalog", "table_catalog", targetIp + "#table_catalog");
                    ftpUtils.uploadFile("catalog", "index_catalog", targetIp + "#index_catalog");

                    System.out.println("迁移完成：" + tableName + " 到 " + targetIp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            file.delete();
        }
    }

    @Override
    public void run() {
        System.out.println("新消息>>>从节点的主服务器监听线程启动！");
        while (isRunning) {
            if (socket.isClosed() || socket.isInputShutdown() || socket.isOutputShutdown()) {
                isRunning = false;
                break;
            }

            try {
                receiveFromMaster();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }
}
