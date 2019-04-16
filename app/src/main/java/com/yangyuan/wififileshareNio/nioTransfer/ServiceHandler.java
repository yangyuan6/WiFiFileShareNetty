package com.yangyuan.wififileshareNio.nioTransfer;

import android.os.Environment;

import com.yangyuan.wififileshareNio.Utils.BufferedRandomAccessFile;
import com.yangyuan.wififileshareNio.Utils.GetServiceFileInfosFromSdUtil;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.bean.TransferFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yangy on 2018/3/24.
 */

public class ServiceHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private RandomAccessFile randomAccessFile;
    private volatile int lastLength = 0;
    private TransferFile transferFile;
    private final String fileInfo;
    private ArrayList<ServiceFileInfo> serviceFileInfos;

    public ServiceHandler() {
        transferFile = new TransferFile();
        fileInfo=getFileInfo();
        this.serviceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelActive(ctx);
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
        if (msg instanceof Integer) {
            start = (Integer) msg;
            if (start != -1) {
                if (randomAccessFile == null) {
                    ctx.close();
                    return;
                }
                randomAccessFile.seek(start);
                int a = (int) (randomAccessFile.length() - start);
                if (a < lastLength) {
                    lastLength = a;
                }
                byte[] bytes = new byte[lastLength];
                if ((byteRead = randomAccessFile.read(bytes)) != -1
                        && (randomAccessFile.length() - start) > 0) {
                    transferFile.setEndPos(byteRead);
                    transferFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(transferFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    randomAccessFile.close();
                    transferFile.setEndPos(-1);
                    ctx.writeAndFlush(transferFile);
                }
            }
        } else if (msg instanceof String) {
            String command = ((String) msg).split("#")[0];
            if (command == null || command.trim().isEmpty()) {
                ctx.writeAndFlush("Error");
            } else if ("GetFileInfo".equals(command)) {
                ctx.writeAndFlush(fileInfo);
                ctx.close();
            } else if ("GetFile".equals(command)) {
                String path = ((String) msg).split("#")[1];
                getFile(ctx,path);
            }else if ("GetHalfFile".equals(command)) {
                String path = ((String) msg).split("#")[1];
                String halfPos = ((String) msg).split("#")[2];
                int halfPos_int=Integer.parseInt(halfPos);
                getHalfFile(ctx,path,halfPos_int);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    private String getFileInfo() {
        BufferedReader br = null;
        String str = "";
        try {
            String filePath = Environment.getExternalStorageDirectory().getPath()
                    + "/WifiSharingSaveDir/shareServiceFileInfo.db";
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
    private void getFile(ChannelHandlerContext ctx,String filePath){
        File file = new File(filePath);
        try {
            randomAccessFile = new BufferedRandomAccessFile(
                    file, "r");

        randomAccessFile.seek(0);
        lastLength = 1024 * 16;
        byte[] bytes = new byte[lastLength];
        start=0;
        if ((byteRead = randomAccessFile.read(bytes)) != -1
                && (randomAccessFile.length() - start) > 0) {
            transferFile.setEndPos(byteRead);
            transferFile.setBytes(bytes);
            transferFile.setFileName(file.getName());
            try {
                ctx.writeAndFlush(transferFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            randomAccessFile.close();
        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void getHalfFile(ChannelHandlerContext ctx,String filePath,int halfPos){
        File file = new File(filePath);
        try {
            randomAccessFile = new RandomAccessFile(
                    file, "r");

            randomAccessFile.seek(halfPos);
            lastLength = 1024 * 16;
            byte[] bytes = new byte[lastLength];
            start=0;
            if ((byteRead = randomAccessFile.read(bytes)) != -1
                    && (randomAccessFile.length() - start) > 0) {
                transferFile.setEndPos(byteRead);
                transferFile.setBytes(bytes);
                transferFile.setFileName(file.getName());
                try {
                    ctx.writeAndFlush(transferFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                randomAccessFile.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}