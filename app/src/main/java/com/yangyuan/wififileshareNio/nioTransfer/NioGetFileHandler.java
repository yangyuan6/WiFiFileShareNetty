package com.yangyuan.wififileshareNio.nioTransfer;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.bean.TransferFile;

import java.io.File;
import java.io.RandomAccessFile;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yangy on 2018/3/24.
 */

public class NioGetFileHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String filePath;
    String baseDir = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/";
    private ServiceFileInfo fileInfo;
    private final int bufferSize=16000;
    private Handler uiHandler;
    private final int SUCCESS=5;
    private final int AllFINISH=6;
    private final int ONEFINISH=1;
    private boolean cancel=false;
    private Message receiveMsg= Message.obtain();
    private long overplusFilelength=0;
    private int transCount=0;
    private Handler oneFinishHandler;
    public  NioGetFileHandler(ServiceFileInfo fileInfo, Handler uiHandler,Handler oneFinishHandler){
        this.fileInfo=fileInfo;
        this.uiHandler=uiHandler;
        this.oneFinishHandler=oneFinishHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
        ctx.writeAndFlush("GetFile#"+fileInfo.getFilepath());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
        ctx.flush();
        ctx.close();
    }


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof TransferFile) {
            TransferFile transferFile = (TransferFile) msg;
            byte[] bytes = transferFile.getBytes();
            byteRead = transferFile.getEndPos();
            String path = baseDir  + transferFile.getFileName();
            File file = new File(path);
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);
            start = start + byteRead;
            receiveMsg=Message.obtain();
            receiveMsg.what=SUCCESS;
            receiveMsg.obj = fileInfo.getUuid();
            overplusFilelength+=bytes.length;
            receiveMsg.arg1=(int)(100-overplusFilelength*100/fileInfo.getTransRange().getEndByte());
            receiveMsg.arg2=(int)(fileInfo.getTransRange().getEndByte()-overplusFilelength);
            uiHandler.sendMessage(receiveMsg);
            transCount++;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);
                randomAccessFile.close();
//                if(byteRead!=1024 * 10){
//                    Thread.sleep(1000);
//                    channelInactive(ctx);
//                }
            } else {
                receiveMsg=Message.obtain();
                receiveMsg.what=SUCCESS;
                oneFinishHandler.sendMessage(receiveMsg);
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}