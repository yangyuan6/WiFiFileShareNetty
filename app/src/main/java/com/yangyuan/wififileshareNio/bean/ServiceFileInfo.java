package com.yangyuan.wififileshareNio.bean;

import android.text.TextUtils;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

/**
 * Created by yangy on 2017/3/1.
 */

public class ServiceFileInfo implements Serializable
{
    private String phoneId;
    private String uuid;
    private String filepath;
    private String fileDesc;
    private String fileName;
    private float sendPercent; //(percent为0-1之间的小数)
    private SendStatus sendStatu = SendStatus.SenddingBegin;
    private Range transRange;
    FileType fileType = FileType.file;

    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    public ServiceFileInfo()
    {
        uuid = UUID.randomUUID().toString();
    }
    public ServiceFileInfo(String uuid)
    {
        this.uuid = uuid;
    }
    public String getUuid()
    {
        return uuid;
    }

    public String getFilepath()
    {
        return filepath;
    }

    public void setFilepath(String filepath)
    {
        this.filepath = filepath;
    }

    public float getSendPercent()
    {
        return sendPercent;
    }

    public void setSendPercent(float sendPercent)
    {
        this.sendPercent = sendPercent;
    }

    public SendStatus getSendStatu()
    {
        return sendStatu;
    }

    public void setSendStatu(SendStatus sendStatu)
    {
        this.sendStatu = sendStatu;
    }

    public FileType getFileType()
    {
        return fileType;
    }

    public void setFileType(FileType fileType)
    {
        this.fileType = fileType;
    }

    public Range getTransRange()
    {
        return transRange;
    }

    public void setTransRange(Range transRange)
    {
        this.transRange = transRange;
    }

    public String getFileDesc()
    {
        return fileDesc;
    }

    public void setFileDesc(String fileDesc)
    {
        this.fileDesc = fileDesc;
    }

    public long getFileLength()
    {
        if(TextUtils.isEmpty(filepath))
            return 0;
        File file = new File(filepath);
        return file.length();
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
