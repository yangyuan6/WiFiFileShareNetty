package com.yangyuan.wififileshareNio.bean;

/**
 * Created by yangy on 2018/3/10.
 */
import java.io.Serializable;

public class TransferFile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fileName;// 文件名
    private byte[] bytes;// 文件字节数组
    private int endPos;// 结尾位置
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public byte[] getBytes() {
        return bytes;
    }
    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
    public int getEndPos() {
        return endPos;
    }
    public void setEndPos(int endPos) {
        this.endPos = endPos;
    }
    public static long getSerialversionuid() {
        return serialVersionUID;
    }
}