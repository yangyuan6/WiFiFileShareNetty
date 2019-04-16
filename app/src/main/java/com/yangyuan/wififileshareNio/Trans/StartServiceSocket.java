package com.yangyuan.wififileshareNio.Trans;


import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;


/**
 * Created by yangy on 2016/12/1.
 */
public class StartServiceSocket implements Runnable{
    ServerSocket serverSocket=null;
   /* private ThreadPool threadPool;*/
    private ExecutorService threadPool;
    public StartServiceSocket(ExecutorService threadPool){
        this.threadPool=threadPool;
    }
    @Override
    public void run() {
        try {
            serverSocket=new ServerSocket(60666);
            while (!serverSocket.isClosed()){
                Socket socket=serverSocket.accept();
                ServiceHandler serviceHandler=new ServiceHandler(socket);
                serviceHandler.setPriority(Thread.MAX_PRIORITY);
                threadPool.execute(serviceHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
