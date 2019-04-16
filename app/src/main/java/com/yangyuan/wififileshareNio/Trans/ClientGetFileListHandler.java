package com.yangyuan.wififileshareNio.Trans;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.Utils.FileUtil;
import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.SendStatus;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.sendReciver.SendStateChangedReciver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by yangy on 2017/3/7.
 */

public class ClientGetFileListHandler extends Thread {
    private Socket socket;
    private BufferedOutputStream bufferedOutputStream;
    private PrintWriter printWriter;
    private String ip;
    private ArrayList<ServiceFileInfo> fileInfos;
    private final int bufferSize=16000;
    private Handler uiHandler;
    private final int SUCCESS=5;
    private final int AllFINISH=6;
    private final int ONEFINISH=1;
    private boolean cancel=false;
    private Message receiveMsg= Message.obtain();
    public  ClientGetFileListHandler(String ip, ArrayList<ServiceFileInfo> fileInfos, Handler uiHandler){
        this.ip=ip;
        this.fileInfos=fileInfos;
        this.uiHandler=uiHandler;
    }
    @Override
    public void run() {
        try {
            socket=new Socket(ip,60666);
            printWriter=new PrintWriter(socket.getOutputStream());
            DataInputStream dataInputStream=new DataInputStream(new BufferedInputStream(socket.getInputStream(),bufferSize));
            String fileIdList="GetFileList";
            String baseDir = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/";
            FileUtil.CreateDirAndFile(baseDir);

            for (ServiceFileInfo serviceFileInfo:fileInfos){
                fileIdList=fileIdList+"###"+serviceFileInfo.getUuid();
            }
            printWriter.println(fileIdList);
            printWriter.flush();
            byte[] buf = new byte[bufferSize];
            for (int i=0;i<fileInfos.size();i++){
                if (cancel){
                    printWriter.println("close");
                    printWriter.close();
                    socket.close();
                    dataInputStream.close();

                    return;
                }
                String fileInfoString = dataInputStream.readUTF();
                String[] fileInfoStrings = fileInfoString.split("###");
                long filelength = Long.parseLong(fileInfoStrings[0]);
                long overplusFilelength=filelength;
                String fileNameString = fileInfoStrings[1];
                String filePath = baseDir+fileNameString;
                if(fileInfos.get(i).getFileType()== FileType.app){
                    filePath = baseDir+fileInfos.get(i).getFileName()+".apk";
                }
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath),bufferSize);//共耗时：1025毫秒
                long start = System.currentTimeMillis();
                int transCount=0;
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
                    if (cancel){
                        printWriter.println("close");
                        bufferedOutputStream.close();
                        printWriter.close();
                        socket.close();
                        dataInputStream.close();
                        bufferedOutputStream.close();
                        return;
                    }
                    overplusFilelength -= bufferSize;
                    transCount++;
/*                    receiveMsg=Message.obtain();
                    receiveMsg.what=SUCCESS;
                    receiveMsg.obj = fileInfos.get(i).getUuid();
                    receiveMsg.arg1=(int)(100-overplusFilelength*100/filelength);
                    receiveMsg.arg2=(int)(filelength-overplusFilelength);
                    uiHandler.sendMessage(receiveMsg);*/
                    if(transCount==6){
                        receiveMsg=Message.obtain();
                        receiveMsg.what=SUCCESS;
                        receiveMsg.obj = fileInfos.get(i).getUuid();
                        receiveMsg.arg1=(int)(100-overplusFilelength*100/filelength);
                        receiveMsg.arg2=(int)(filelength-overplusFilelength);
                        uiHandler.sendMessage(receiveMsg);
                        transCount=0;
                    }
                }
                bufferedOutputStream.close();
                receiveMsg=Message.obtain();
                receiveMsg.what=ONEFINISH;
                receiveMsg.obj = fileInfos.get(i).getUuid();
                receiveMsg.arg1=100;
                receiveMsg.arg2=(int)filelength;
                uiHandler.sendMessage(receiveMsg);
                long end = System.currentTimeMillis();
                String msg1=bufferSize+" :"+"共用时"+(end-start)/1000.000000+"s";
                BaseApplication.showToast(msg1);
            }
            receiveMsg=Message.obtain();
            receiveMsg.what=AllFINISH;
            receiveMsg.obj = 100;
            uiHandler.sendMessage(receiveMsg);
            SendStateChangedReciver.sendStatuChangedBroadcast("", SendStatus.AllFinish, 1);
            printWriter.close();
            dataInputStream.close();
            socket.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }
}
