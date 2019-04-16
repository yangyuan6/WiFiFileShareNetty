package com.yangyuan.wififileshareNio.Trans;

import android.os.Handler;
import android.os.Message;

import com.yangyuan.wififileshareNio.reciver.GetShareFileInfosReciver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

/**
 * Created by yangy on 2017/3/3.
 */

public class ClientGetFileInfosHandler extends Thread {
    private Socket socket;
    private InetAddress address;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    private Handler uiHandler;
    private Message receiveMsg= Message.obtain();
    protected  final int GETINFOS_SUCCESS = 1;
    public ClientGetFileInfosHandler(InetAddress address, Handler uiHandler){
        this.address=address;
        this.uiHandler=uiHandler;
    }
    public ClientGetFileInfosHandler(InetAddress address){
        this.address=address;
    }
    @Override
    public void run() {
        try {
            SocketAddress socketAddress = new InetSocketAddress(address, 60666);
            socket=new Socket();
            socket.connect(socketAddress, 15000);
            printWriter=new PrintWriter(socket.getOutputStream());
            bufferedReader=new BufferedReader(new InputStreamReader(socket.getInputStream()));
            if(!socket.isClosed()){
                printWriter.println("GetFileInfo");
                printWriter.flush();
                String getShareFileInfos=bufferedReader.readLine();
                printWriter.println("bye");
                printWriter.flush();
                receiveMsg.what=GETINFOS_SUCCESS;
                receiveMsg.obj = getShareFileInfos;
                uiHandler.sendMessage(receiveMsg);
                printWriter.close();
                bufferedReader.close();
                socket.close();
                GetShareFileInfosReciver.sendGetShareFileInfosBroadcast(getShareFileInfos);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
