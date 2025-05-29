package RegionManagers.SockectManager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FtpUtils {
    public String hostname = "10.162.251.198";
    public int port = 21;
    public String username = "LeChatelierLenz";
    public String password = "031716cqp";
    private static final int BUFFER_SIZE = 1024 * 1024 * 4;
    public FTPClient ftpClient = null;

    private void login() {
        ftpClient = new FTPClient();
        ftpClient.setControlEncoding("utf-8");
        try {
            ftpClient.connect(hostname, port);
            ftpClient.login(username, password);
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(BUFFER_SIZE);
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                closeConnect();
                System.out.println("FTP服务器连接失败");
            }
            System.out.println("FTP连接成功");
        } catch (Exception e) {
            System.out.println("FTP登录失败" + e.getMessage());
        }
    }

    private void closeConnect() {
        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                System.out.println("关闭FTP连接失败" + e.getMessage());
            }
        }
    }

    public Boolean downLoadFile(String ftpPath, String fileName, String savePath) {
        login();
        OutputStream os = null;
        if (ftpClient != null) {
            try {
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println("/" + ftpPath + "该目录不存在");
                    return false;
                }
                ftpClient.enterLocalPassiveMode();
                FTPFile[] ftpFiles = ftpClient.listFiles();

                if (ftpFiles == null || ftpFiles.length == 0) {
                    System.out.println("/" + ftpPath + "该目录下无文件");
                    return false;
                }
                for (FTPFile file : ftpFiles) {
                    if (fileName.equals("") || fileName.equalsIgnoreCase(file.getName())) {
                        if (!file.isDirectory()) {
                            File saveFile = new File(savePath + file.getName());
                            os = new FileOutputStream(saveFile);
                            ftpClient.retrieveFile(file.getName(), os);
                            os.close();
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                System.out.println("下载文件失败" + e.getMessage());
            } finally {
                if (null != os) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return false;
    }

    public Boolean downLoadblankFile() {
        login();
        OutputStream os = null;
        String ftpPath = "blank";
        String savePath = "";

        if (ftpClient != null) {
            try {
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println("/" + ftpPath + " 该目录不存在");
                    return false;
                }

                ftpClient.enterLocalPassiveMode();
                FTPFile[] ftpFiles = ftpClient.listFiles();

                if (ftpFiles == null || ftpFiles.length == 0) {
                    System.out.println("/" + ftpPath + " 该目录下无文件");
                    return false;
                }

                // 创建本地保存目录（如果不存在）
                File localDir = new File(savePath);
                if (!localDir.exists()) {
                    localDir.mkdirs();
                }

                for (FTPFile file : ftpFiles) {
                    if (!file.isDirectory()) {
                        String fileName = file.getName();
                        File saveFile = new File(savePath + fileName);
                        os = new FileOutputStream(saveFile);
                        boolean success = ftpClient.retrieveFile(fileName, os);
                        os.close();

                        if (success) {
                            System.out.println("成功下载文件：" + fileName);
                        } else {
                            System.out.println("下载失败：" + fileName);
                        }
                    }
                }
                System.out.println("成功下载全部空白表单");
                return true;
            } catch (IOException e) {
                System.out.println("下载文件失败：" + e.getMessage());
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return false;
    }


    public boolean downloadtableFile(String ftpPath, String targetIP, String fileName, String savePath) {
        login();
        OutputStream os = null;
        fileName = targetIP + "#" + fileName;
        if (ftpClient != null) {
            try {
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println("/" + ftpPath + "该目录不存在");
                    return false;
                }
                ftpClient.enterLocalPassiveMode();
                FTPFile[] ftpFiles = ftpClient.listFiles();

                if (ftpFiles == null || ftpFiles.length == 0) {
                    System.out.println("/" + ftpPath + "该目录下无文件");
                    return false;
                }
                for (FTPFile file : ftpFiles) {
                    if (fileName.equals("") || fileName.equalsIgnoreCase(file.getName())) {
                        if (!file.isDirectory()) {
                            String realFileName = file.getName().substring(file.getName().indexOf("#") + 1);
                            File saveFile = new File(savePath + realFileName);
                            os = new FileOutputStream(saveFile, true);
                            ftpClient.retrieveFile(file.getName(), os);
                            os.close();
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                System.out.println("下载文件失败" + e.getMessage());
            } finally {
                if (null != os) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return false;
    }

    public boolean downloadtable1File(String ftpPath, String targetIP, String fileName, String savePath) {
        login();
        OutputStream os = null;
        fileName = targetIP + "#" + fileName;
        if (ftpClient != null) {
            try {
                if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                    System.out.println("/" + ftpPath + "该目录不存在");
                    return false;
                }
                ftpClient.enterLocalPassiveMode();
                FTPFile[] ftpFiles = ftpClient.listFiles();

                if (ftpFiles == null || ftpFiles.length == 0) {
                    System.out.println("/" + ftpPath + "该目录下无文件");
                    return false;
                }
                for (FTPFile file : ftpFiles) {
                    if (fileName.equals("") || fileName.equalsIgnoreCase(file.getName())) {
                        if (!file.isDirectory()) {
                            String realFileName = file.getName().substring(file.getName().indexOf("#") + 1);
                            String realFileNames=realFileName+"a";
                            File saveFile = new File(savePath + realFileNames);
                            os = new FileOutputStream(saveFile, true);
                            ftpClient.retrieveFile(file.getName(), os);
                            mergeFFMessages(realFileName,realFileNames,realFileName);
                            add2ToByte21("table_catalog");
                            os.close();
                        }
                    }
                }
                return true;
            } catch (IOException e) {
                System.out.println("下载文件失败" + e.getMessage());
            } finally {
                if (null != os) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return false;
    }

    public boolean downLoadcatalogFile() {
        String fileName1 = "table_catalog";
        String fileName2 = "index_catalog";
        String savePath = "catalog";
        boolean download_table = downLoadFile(fileName1, savePath,"");
        boolean download_index = downLoadFile(fileName2, savePath,"");
        if (!download_table && !download_index) {
            return false;
        }
        System.out.println("下载目录文件成功");
        return true;
    }

    public boolean upLoadFile(String fileName, String savePath) {
        login();
        boolean flag = false;
        InputStream inputStream = null;
        if (ftpClient != null) {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    System.out.println("本地文件不存在：" + fileName);
                    return false;
                }
                inputStream = new FileInputStream(file);
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.makeDirectory(savePath);
                ftpClient.changeWorkingDirectory(savePath);
                boolean done = ftpClient.storeFile(fileName, inputStream);
                if (!done) {
                    System.out.println("上传失败：" + fileName);
                }
                inputStream.close();
                System.out.println("创建FTP文件： " + fileName);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return flag;
    }

    public boolean upLoadblankFile(String fileName) {
        login();
        String savePath = "blank";
        boolean flag = false;
        InputStream inputStream = null;

        if (ftpClient != null) {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    System.out.println("本地文件不存在：" + fileName);
                    return false;
                }

                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.makeDirectory(savePath);
                ftpClient.changeWorkingDirectory(savePath);
                // 获取当前目录下的文件名列表
                String remoteFileName = file.getName();
                FTPFile[] files = ftpClient.listFiles(remoteFileName);

                if (files != null && files.length > 0) {
                    System.out.println("不需要上传，文件已存在：" + remoteFileName);
                    flag = true;
                } else {
                    inputStream = new FileInputStream(file);
                    boolean done = ftpClient.storeFile(remoteFileName, inputStream);
                    if (done) {
                        System.out.println("成功上传空白表单：" + remoteFileName);
                        flag = true;
                    } else {
                        System.out.println("上传失败：" + remoteFileName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }

        return flag;
    }


    public boolean upLoadtableFile(String fileName, String localIP, String savePath) {
        login();
        boolean flag = false;
        InputStream inputStream = null;
        if (ftpClient != null) {
            try {
                File file = new File(fileName);
                if (!file.exists()) {
                    System.out.println("本地文件不存在：" + fileName);
                    return false;
                }
                inputStream = new FileInputStream(file);
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.makeDirectory(savePath);
                ftpClient.changeWorkingDirectory(savePath);
                boolean done = ftpClient.storeFile(fileName, inputStream);
                if (!done) {
                    System.out.println("上传失败：" + fileName);
                }
                ftpClient.rename(fileName, localIP + "#" + fileName);
                inputStream.close();
                System.out.println("创建FTP文件： " + fileName);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != inputStream) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                closeConnect();
            }
        }
        return flag;
    }

    public boolean upLoadcatalogFile() {
        String fileName1 = "table_catalog";
        String fileName2 = "index_catalog";
        String savePath = "catalog";
        boolean upload_table = upLoadFile(fileName1, savePath);
        boolean upload_index = upLoadFile(fileName2, savePath);
        if (!upload_table && !upload_index) {
            return false;
        }
        System.out.println("上传目录文件成功");
        return true;
    }

    public boolean deleteFile(String fileName,String localIP ,String filePath) {
        login();
        boolean flag = false;
        fileName=localIP + "#" + fileName;
        if (ftpClient != null) {
            try {
                ftpClient.changeWorkingDirectory(filePath);
                ftpClient.dele(fileName);
                System.out.println("删除FTP文件： " + fileName);
                flag = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeConnect();
            }
        }
        return flag;
    }



    public static void mergeFFMessages(String file1Path, String file2Path, String outputPath) throws IOException {
        byte[] file1Bytes = readAllBytes(file1Path);
        byte[] file2Bytes = readAllBytes(file2Path);
        if (file1Bytes.length < 4 || !(file1Bytes[0] == (byte) 0xFF && file1Bytes[1] == (byte) 0xFF &&
                file1Bytes[2] == (byte) 0xFF && file1Bytes[3] == (byte) 0xFF)) {
            throw new IllegalArgumentException("File 1 does not start with FF FF FF FF header.");
        }

        // 读取头
        byte[] header = new byte[4];
        System.arraycopy(file1Bytes, 0, header, 0, 4);

        // 提取消息：跳过前4字节
        List<byte[]> messages = new ArrayList<>();
        messages.addAll(extractFFMessages(file1Bytes, 4));
        messages.addAll(extractFFMessages(file2Bytes, 4));

        // 准备输出
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        output.write(header);

        for (byte[] msg : messages) {
            if (output.size() + 15 > 4096) break; // 超出4096则停止
            output.write(msg);
        }

        // 补0
        int padding = 4096 - output.size();
        if (padding > 0) {
            output.write(new byte[padding]);
        }

        // 写入
        try (FileOutputStream fos = new FileOutputStream(outputPath)) {
            output.writeTo(fos);
        }

        System.out.println("合并完成，输出大小：" + new File(outputPath).length() + " 字节");
    }

    // 提取以0xFF开头、长度15的消息
    private static List<byte[]> extractFFMessages(byte[] data, int start) {
        List<byte[]> messages = new ArrayList<>();
        for (int i = start; i <= data.length - 15; i++) {
            if ((data[i] & 0xFF) == 0xFF) {
                byte[] msg = new byte[15];
                System.arraycopy(data, i, msg, 0, 15);
                messages.add(msg);
                i += 14; // 跳过当前消息
            }
        }
        return messages;
    }

    private static byte[] readAllBytes(String path) throws IOException {
        File file = new File(path);
        byte[] buffer = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(buffer);
        }
        return buffer;
    }

    public static void add2ToByte21(String filePath) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(filePath, "rw");

        if (raf.length() < 21) {
            raf.close();
            throw new IOException("文件长度不足 21 字节");
        }

        raf.seek(21);  // 第 21 字节，索引从 0 开始
        int original = raf.readUnsignedByte();  // 读取原始值
        int modified = (original + 2) & 0xFF;   // 保持字节范围在 0~255
        raf.seek(21);
        raf.write(modified);

        raf.close();
        System.out.println("第 21 字节已从 " + original + " 修改为 " + modified);
    }
}
