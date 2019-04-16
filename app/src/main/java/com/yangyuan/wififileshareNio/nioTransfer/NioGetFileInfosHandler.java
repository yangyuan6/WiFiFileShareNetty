package com.yangyuan.wififileshareNio.nioTransfer;

import android.os.Handler;
import android.os.Message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yangy on 2018/3/24.
 */

public class NioGetFileInfosHandler extends ChannelInboundHandlerAdapter {
    private Handler uiHandler;
    private Message receiveMsg = Message.obtain();
    protected  final int GETINFOS_SUCCESS = 1;
    public NioGetFileInfosHandler(Handler uiHandler) {
        this.uiHandler=uiHandler;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Auto-generated method stub
        super.channelInactive(ctx);
    }

    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("GetFileInfo");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof String) {
            receiveMsg.what=GETINFOS_SUCCESS;
            receiveMsg.obj = msg;
            uiHandler.sendMessage(receiveMsg);
        }
        ctx.close();
        ctx.channel().eventLoop().shutdownGracefully();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
        ctx.channel().eventLoop().shutdownGracefully();
    }
}