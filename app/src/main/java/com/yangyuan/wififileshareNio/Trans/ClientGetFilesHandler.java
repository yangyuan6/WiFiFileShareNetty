package com.yangyuan.wififileshareNio.Trans;

import android.os.Environment;

import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.bean.SendStatus;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.sendReciver.SendStateChangedReciver;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by yangy on 2017/3/3.
 */

public class ClientGetFilesHandler extends Thread {
    private Socket socket;
    private BufferedOutputStream bufferedOutputStream;
    private PrintWriter printWriter;
    private String ip;
    private ArrayList<ServiceFileInfo> fileInfos;
    public  ClientGetFilesHandler(String ip, ArrayList<ServiceFileInfo> fileInfos){
        this.ip=ip;
        this.fileInfos=fileInfos;
    }

    @Override
    public void run() {
        try {
            socket=new Socket(ip,60666);
            printWriter=new PrintWriter(socket.getOutputStream());
            DataInputStream dataInputStream=new DataInputStream(socket.getInputStream());
            for (ServiceFileInfo fileinfo:fileInfos) {
                /*bufferedInputStream=new BufferedInputStream(socket.getInputStream(),65536);*/
                File file=new File(fileinfo.getFilepath());
                if (file.isFile() && file.exists()){
                    file.delete();
                }
                String baseDir = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/";
                printWriter.println("GetShareFile###"+fileinfo.getUuid());
                printWriter.flush();
                String tempString= dataInputStream.readUTF();
                String[] strs1 = tempString.split("###");
                long filelength = Long.parseLong(strs1[0]);
                long overplusFilelength=filelength;
                baseDir += strs1[1];
                String filePath = baseDir;
                bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(filePath),65536);
                int bufferSize = 8192;
                byte[] buf = new byte[bufferSize];
                long start = System.currentTimeMillis();
                float overplusFilelength_float;
                float filelength_float;
                while (true) {
                    if (overplusFilelength >= bufferSize) {
                        dataInputStream.readFully(buf);
                        bufferedOutputStream.write(buf, 0, bufferSize);
                    } else {
                        byte[] smallBuf = new byte[(int) overplusFilelength];
                        BaseApplication.showToast(overplusFilelength+"");
                        dataInputStream.readFully(smallBuf);
                        bufferedOutputStream.write(smallBuf);
                        break;
                    }
                    overplusFilelength -= bufferSize;
                    filelength_float=  Float.parseFloat(filelength+"");
                    overplusFilelength_float=Float.parseFloat(overplusFilelength+"");
                    SendStateChangedReciver.sendStatuChangedBroadcast(fileinfo.getUuid(), SendStatus.PercentChange, 1-overplusFilelength_float/filelength_float);

                }
                bufferedOutputStream.flush();
                bufferedOutputStream.close();
                SendStateChangedReciver.sendStatuChangedBroadcast(fileinfo.getUuid(), SendStatus.PercentChange, 1);


                long end = System.currentTimeMillis();
                String msg1="文件接收成功,共用时"+(end-start)/1000.00+"s";
                System.err.println(msg1);
                /*receiveMsg.what = FIFESUCCESS;
                receiveMsg.obj = msg1;
                uiHandler.sendMessage(receiveMsg);*/

            }
            SendStateChangedReciver.sendStatuChangedBroadcast("", SendStatus.AllFinish, 1);
            printWriter.println("bye");
            printWriter.close();
            dataInputStream.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
