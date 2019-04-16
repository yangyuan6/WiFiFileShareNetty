package com.yangyuan.wififileshareNio.Trans;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.google.gson.Gson;
import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.Utils.BufferedRandomAccessFile;
import com.yangyuan.wififileshareNio.Utils.FileUtil;
import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.SendStatus;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.sendReciver.SendStateChangedReciver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

import static com.yangyuan.wififileshareNio.config.Config.bufferSize;

/**
 * Created by yangy on 2018/1/5.
 */

public class ClientGetHalfFileHandler extends Thread {
    private String ip;
    private ArrayList<ServiceFileInfo> fileInfos;
    private Handler uiHandler;
    private Message receiveMsg = Message.obtain();
    private final int SUCCESS = 5;
    private final int AllFINISH = 6;
    private final int ONEFINISH = 1;
    private boolean cancel = false;

    public ClientGetHalfFileHandler(String ip, ArrayList<ServiceFileInfo> fileInfos, Handler uiHandler) {
        this.ip = ip;
        this.fileInfos = fileInfos;
        this.uiHandler = uiHandler;
    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket(ip, 60666);
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(socket.getInputStream(), bufferSize));
            String fileIdList = "GetHalfFile";
            String baseDir = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/";
            FileUtil.CreateDirAndFile(baseDir);
            String fileInfosJson=new Gson().toJson(fileInfos);
           /* for (HalfFileInfo serviceFileInfo : fileInfos) {
                fileIdList = fileIdList + "###" + serviceFileInfo.getUuid();
            }*/
           fileIdList=fileIdList+"###"+fileInfosJson;
            printWriter.println(fileIdList);
            printWriter.flush();
            byte[] buf = new byte[bufferSize];
            for (int i = 0; i < fileInfos.size(); i++) {
                if (cancel) {
                    break;
                }
                if(i==0){
                    String filePath=fileInfos.get(0).getFilepath();
                    if (!filePath.contains("WifiSharingSaveDir")){
                        filePath=baseDir+fileInfos.get(0).getFileName();
                    }
                    File file=new File(filePath);
                    if(!file.exists()){
                        file.createNewFile();
                    }
                    BufferedRandomAccessFile bufferedRandomAccessFile=new BufferedRandomAccessFile(file,"rw");
                    bufferedRandomAccessFile.seek(fileInfos.get(0).getTransRange().getBeginByte());
                    long filelength=fileInfos.get(0). getTransRange().getEndByte();
                    long overplusFilelength =fileInfos.get(0). getTransRange().getEndByte()-fileInfos.get(0).getTransRange().getBeginByte();
                    long start = System.currentTimeMillis();
                    int transCount=0;
                    while (true){
                        if (overplusFilelength >= bufferSize) {
                            dataInputStream.readFully(buf);
                            bufferedRandomAccessFile.write(buf, 0, bufferSize);
                        } else {
                            byte[] smallBuf = new byte[(int) overplusFilelength];
                            dataInputStream.readFully(smallBuf);
                            bufferedRandomAccessFile.write(smallBuf);
                            break;
                        }
                        if (cancel) {
                            dataInputStream.close();
                            bufferedRandomAccessFile.close();
                            socket.close();
                            break;
                        }
                        overplusFilelength -= bufferSize;
                        transCount++;
                        if(transCount==6)
                        {
                            transCount=0;
                            receiveMsg = Message.obtain();
                            receiveMsg.what = SUCCESS;
                            receiveMsg.obj = fileInfos.get(i).getUuid();
                            receiveMsg.arg2=(int)(filelength-overplusFilelength);
                            receiveMsg.arg1 = (int) (100 - overplusFilelength * 100 / filelength);
                            uiHandler.sendMessage(receiveMsg);
                        }

                    }
                    bufferedRandomAccessFile.close();
                    receiveMsg = Message.obtain();
                    receiveMsg.what = ONEFINISH;
                    receiveMsg.obj = fileInfos.get(i).getUuid();
                    receiveMsg.arg1 = 100;
                    receiveMsg.arg2=(int)overplusFilelength;
                    uiHandler.sendMessage(receiveMsg);
                    long end = System.currentTimeMillis();
                    String msg1 = bufferSize + " :" + "共用时" + (end - start) / 1000.000000 + "s";
                    BaseApplication.showToast(msg1);
                    continue;
                }
                String fileInfoString = dataInputStream.readUTF();
                String[] fileInfoStrings = fileInfoString.split("###");
                long filelength = Long.parseLong(fileInfoStrings[0]);
                long overplusFilelength = filelength;
                String fileNameString = fileInfoStrings[1];
                String filePath = baseDir + fileNameString;
                if (fileInfos.get(i).getFileType() == FileType.app) {
                    filePath = baseDir + fileInfos.get(i).getFileName() + ".apk";
                }
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath), bufferSize);
                long start = System.currentTimeMillis();
                int transCount = 0;
                while (true) {
                    if (overplusFilelength >= bufferSize) {
                        dataInputStream.readFully(buf);
                        bufferedOutputStream.write(buf, 0, bufferSize);
                    } else {
                        byte[] smallBuf = new byte[(int) overplusFilelength];
                        dataInputStream.readFully(smallBuf);
                        bufferedOutputStream.write(smallBuf);
                        break;
                    }
                    if (cancel) {
                        printWriter.println("close");
                        printWriter.close();
                        dataInputStream.close();
                        socket.close();
                        bufferedOutputStream.close();
                        break;
                    }
                    overplusFilelength -= bufferSize;
                   /* transCount++;*/
                    receiveMsg = Message.obtain();
                    receiveMsg.what = SUCCESS;
                    receiveMsg.obj = fileInfos.get(i).getUuid();
                    receiveMsg.arg1 = (int) (100 - overplusFilelength * 100 / filelength);
                    receiveMsg.arg2=(int)overplusFilelength;
                    uiHandler.sendMessage(receiveMsg);
                    /*if (transCount == 10) {
                        receiveMsg = Message.obtain();
                        receiveMsg.what = SUCCESS;
                        receiveMsg.obj = fileInfos.get(i).getUuid();
                        receiveMsg.arg1 = (int) (100 - overplusFilelength * 100 / filelength);
                        uiHandler.sendMessage(receiveMsg);
                        transCount = 0;
                    }*/
                }
                bufferedOutputStream.close();
                receiveMsg = Message.obtain();
                receiveMsg.what = ONEFINISH;
                receiveMsg.obj = fileInfos.get(i).getUuid();
                receiveMsg.arg1 = 100;
                receiveMsg.arg2=(int)filelength;
                uiHandler.sendMessage(receiveMsg);
                long end = System.currentTimeMillis();
                String msg1 = bufferSize + " :" + "共用时" + (end - start) / 1000.000000 + "s";
                BaseApplication.showToast(msg1);
            }
            receiveMsg = Message.obtain();
            receiveMsg.what = AllFINISH;
            receiveMsg.obj = 100;
            uiHandler.sendMessage(receiveMsg);
            SendStateChangedReciver.sendStatuChangedBroadcast("", SendStatus.AllFinish, 1);
            printWriter.close();
            dataInputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
