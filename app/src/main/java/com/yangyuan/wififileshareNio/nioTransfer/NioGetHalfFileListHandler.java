package com.yangyuan.wififileshareNio.nioTransfer;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.yangyuan.wififileshareNio.Utils.BufferedRandomAccessFile;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.bean.TransferFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yangy on 2018/3/24.
 */

public class NioGetHalfFileListHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String filePath;
    String baseDir = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/";
    private ArrayList<ServiceFileInfo> fileInfos;
    private int downFileIndex=0;
    private final int bufferSize=16000;
    private Handler uiHandler;
    private final int SUCCESS=5;
    private final int AllFINISH=6;
    private final int ONEFINISH=1;
    private boolean cancel=false;
    private Message receiveMsg= Message.obtain();
    private long overplusFilelength=0;
    private int transCount=0;
    public NioGetHalfFileListHandler(ArrayList<ServiceFileInfo> fileInfos, Handler uiHandler){
        this.fileInfos=fileInfos;
        this.uiHandler=uiHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
        ctx.writeAndFlush("GetHalfFile#"+fileInfos.get(downFileIndex).getUuid()+"#"+fileInfos.get(downFileIndex).getTransRange().getEndByte());

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
            RandomAccessFile randomAccessFile = new BufferedRandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);
            start = start + byteRead;
            receiveMsg=Message.obtain();
            receiveMsg.what=SUCCESS;
            receiveMsg.obj = fileInfos.get(downFileIndex).getUuid();
            overplusFilelength+=bytes.length;
            receiveMsg.arg1=(int)(start*100/fileInfos.get(downFileIndex).getTransRange().getEndByte());
            receiveMsg.arg2=(int)(fileInfos.get(downFileIndex).getTransRange().getEndByte()-start);
            uiHandler.sendMessage(receiveMsg);
            transCount++;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);
                randomAccessFile.close();
            } else {
                receiveMsg=Message.obtain();
                receiveMsg.what=ONEFINISH;
                receiveMsg.obj = fileInfos.get(downFileIndex).getUuid();
                uiHandler.sendMessage(receiveMsg);
                start=0;
                downFileIndex++;
                if (downFileIndex==fileInfos.size()){
                    receiveMsg=Message.obtain();
                    receiveMsg.what=AllFINISH;
                    uiHandler.sendMessage(receiveMsg);
                    ctx.close();
                    return;
                }
                ctx.writeAndFlush("GetFile#"+fileInfos.get(downFileIndex).getFilepath());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}