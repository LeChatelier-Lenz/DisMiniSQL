package RegionManagers.SockectManager;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

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
    public final String MASTER = "10.162.251.198";

    public MasterSocketManager() throws IOException {
        this.socket = new Socket(MASTER, SERVER_PORT);
        this.ftpUtils = new FtpUtils();
        this.dataBaseManager = new DataBaseManager();
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        output = new PrintWriter(socket.getOutputStream(), true);
        isRunning = true;
        //text_function();
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
                // 容灾恢复格式: <master>[3]ip#name@name@...
                System.out.println("收到指令-容灾回复");
                String info = line.substring(11);
                if(line.length()==11) return;
                String ip = info.split("#")[0];
                String[] tables = info.split("#")[1].split("@");
                for(String table : tables) {
                    delFile(table);
                    delFile(table + "_index.index");
                    ftpUtils.downloadtableFile("table", ip,table, "");
                    System.out.println("success " + table);
                    ftpUtils.downloadtableFile("index", ip, table + "_index.index", "");
                    System.out.println("success " + table + "_index.index");
                }

                //ftpUtils.additionalDownloadFile("catalog", ip + "#table_catalog");
                //ftpUtils.additionalDownloadFile("catalog", ip + "#index_catalog");
                try {
                    API.initial();
                    System.out.println("DownloadtableFile finished and initial finished");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println("here");
                output.println("<region>[3]Complete disaster recovery");

            } else if (line.equals("<master>[4]recover")) {
                // 节点初始化
                String tableName = dataBaseManager.getMetaInfo();
                String[] tableNames = tableName.split(" ");
                for (String table : tableNames) {
                    Interpreter.interpret("drop table " + table + " ;");
                }
                initial();

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

                    ftpUtils.upLoadtableFile(tableName,targetIp,"table");
                    ftpUtils.upLoadtableFile(tableName + "_index.index", targetIp,"index");

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

     public void text_function() {
        String IP=socket.getLocalAddress().getHostAddress();
        //ftpUtils.additionaluploadFile("text.txt",IP,"");
        //ftpUtils.additionalDownloadFile("",IP, "text.txt","");
    }

    public void initial(){
        ftpUtils.downLoadblankFile();
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

    public String getMyIP() {
        return socket.getLocalAddress().getHostAddress();
    }
}
