package com.yangyuan.wififileshareNio.Trans;

import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yangyuan.wififileshareNio.Utils.BufferedRandomAccessFile;
import com.yangyuan.wififileshareNio.Utils.GetServiceFileInfosFromSdUtil;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangy on 2017/3/4.
 */

public class ServiceHandler extends Thread {
    protected Socket serviceSocket;
    private BufferedReader bufferedReader;
    private ArrayList<ServiceFileInfo> serviceFileInfos;
    private PrintWriter printWriter;
    private DataOutputStream dataOutputStream;
    private final int bufferSize = 16384;
    private final String fileInfo;


    public ServiceHandler(Socket serviceSocket) {
        this.serviceSocket = serviceSocket;
        this.serviceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
        fileInfo=getFileInfo();
    }

    @Override
    public void run() {
        String msg = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(serviceSocket.getInputStream()));
            printWriter = new PrintWriter(serviceSocket.getOutputStream());
            while (!"bye".equals(msg = bufferedReader.readLine())) {
                String[] strs = msg.split("###");
                System.err.println(msg);
                if ("GetFileInfo".equals(strs[0])) {
                    printWriter.println(fileInfo);
                    printWriter.flush();
                } else if ("GetFile".equals(strs[0])) {
                    String fileid = strs[1];
                    String filePath = null;
                    for (ServiceFileInfo serviceFileInfo : serviceFileInfos) {
                        if (fileid.equals(serviceFileInfo.getUuid())) {
                            filePath = serviceFileInfo.getFilepath();
                            break;
                        }
                    }
                    DownFile(filePath);
                } else if ("GetFileList".equals(strs[0])) {
                    ArrayList<String> fileIdList = new ArrayList<>();
                    for (int i = 1; i < strs.length; i++) {
                        fileIdList.add(strs[i]);
                    }
                    DownFileList(fileIdList);
                } else if ("GetHalfFile".equals(strs[0])) {
                    //BaseApplication.showToast(strs[1]);
                    Gson gson = new Gson();
                    ArrayList<ServiceFileInfo> fileIdList = gson.fromJson(strs[1], new TypeToken<List<ServiceFileInfo>>() {
                    }.getType());
                    DownHalfFileList(fileIdList);
                }
            }

        } catch (Exception e) {
        } finally {
            try {
                printWriter.close();
                bufferedReader.close();
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                serviceSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void DownFileList(ArrayList<String> fileIdList) {
        try {
            for (String fileId : fileIdList) {
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(serviceSocket.getOutputStream(), bufferSize);
                dataOutputStream = new DataOutputStream(bufferedOutputStream);
                String filePath = null;
                for (int i = 0; i < serviceFileInfos.size(); i++) {
                    if (fileId.equals(serviceFileInfos.get(i).getUuid())) {
                        filePath = serviceFileInfos.get(i).getFilepath();
                        break;
                    }
                }
                File fi = new File(filePath);
                String fileInfoString = fi.getName();
                long fileLength = fi.length();
                fileInfoString = fileLength + "###" + fileInfoString;
                dataOutputStream.writeUTF(fileInfoString);
                dataOutputStream.flush();
                DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath), bufferSize));
                byte[] buf = new byte[bufferSize];
                while (true) {
                    int read = 0;
                    if (fis != null) {
                        read = fis.read(buf);
                    }
                    if (read == -1) {
                        break;
                    }
                    dataOutputStream.write(buf, 0, read);
                }
                dataOutputStream.flush();
                bufferedOutputStream.flush();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void DownHalfFileList(ArrayList<ServiceFileInfo> serviceFileInfos) {
        try {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(serviceSocket.getOutputStream(), bufferSize);
            dataOutputStream = new DataOutputStream(bufferedOutputStream);
            int j = 0;
            for (ServiceFileInfo serviceFileInfo : serviceFileInfos) {
                if (j == 0) {
                    BufferedRandomAccessFile bufferedRandomAccessFile = new BufferedRandomAccessFile(new File(serviceFileInfo.getFilepath()), "r");
                    bufferedRandomAccessFile.seek(serviceFileInfo.getTransRange().getBeginByte());
                    byte[] buf = new byte[bufferSize];
                    while (true) {
                        int read = 0;
                        if (bufferedRandomAccessFile != null) {
                            read = bufferedRandomAccessFile.read(buf);
                        }
                        if (read == -1) {
                            break;
                        }
                        dataOutputStream.write(buf, 0, read);
                    }
                    dataOutputStream.flush();
                    bufferedRandomAccessFile.close();
                }

                String filePath = null;
                for (int i = 0; i < serviceFileInfos.size(); i++) {
                    if (serviceFileInfo.equals(serviceFileInfos.get(i).getUuid())) {
                        filePath = serviceFileInfos.get(i).getFilepath();
                        break;
                    }
                }
                File fi = new File(filePath);
                String fileInfoString = fi.getName();
                long fileLength = fi.length();
                fileInfoString = fileLength + "###" + fileInfoString;
                dataOutputStream.writeUTF(fileInfoString);
                dataOutputStream.flush();
                DataInputStream fis = new DataInputStream(new BufferedInputStream(new FileInputStream(filePath), bufferSize));
                byte[] buf = new byte[bufferSize];
                while (true) {
                    int read = 0;
                    if (fis != null) {
                        read = fis.read(buf);
                    }
                    if (read == -1) {
                        break;
                    }
                    dataOutputStream.write(buf, 0, read);
                }
                dataOutputStream.flush();
                bufferedOutputStream.flush();
                fis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getFileInfo() {
        BufferedReader br = null;
        String str = "";
        try {
            String filePath = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/shareServiceFileInfo.db";
            File file = new File(filePath);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), "UTF-8");
            br = new BufferedReader(isr, 16384);

            String mimeTypeLine = null;
            while ((mimeTypeLine = br.readLine()) != null) {
                str = str + mimeTypeLine;
            }
            br.close();
        } catch (FileNotFoundException e) {
            return "FileNotFound";
        } catch (Exception e) {
            return "";
        } finally {
            try {
                br.close();
            } catch (Exception e) {

            } finally {
            }
        }
        return str;
    }

    private void DownFile(String filePath) {
        try {
            File fi = new File(filePath);
            dataOutputStream = new DataOutputStream(new BufferedOutputStream(serviceSocket.getOutputStream(), bufferSize));
            BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(filePath), bufferSize);

            String fileInfoString = fi.getName();
            long fileLength = fi.length();
            fileInfoString = fileLength + "###" + fileInfoString;
            dataOutputStream.writeUTF(fileInfoString);
            dataOutputStream.flush();
            byte[] buf = new byte[bufferSize];
            while (true) {
                int read = 0;
                if (bufferedInputStream != null) {
                    read = bufferedInputStream.read(buf);
                }
                if (read == -1) {
                    dataOutputStream.flush();
                    break;
                }
                dataOutputStream.write(buf, 0, read);
            }
            dataOutputStream.flush();
            bufferedInputStream.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

