package RegionManagers.SockectManager;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;

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
}
