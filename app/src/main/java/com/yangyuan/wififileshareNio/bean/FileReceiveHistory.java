package com.yangyuan.wififileshareNio.bean;

import java.util.Date;
import java.util.UUID;

/**
 * Created by yangy on 2017/3/14.
 */

public class FileReceiveHistory {
    private String id;
    private String fileName;
    private Date insertDate;
    private String path;
    private String desc;
    private String fileType;
    private long size;
    private Boolean isExists;
    public void FileReceiveHistory(){
       this.id= UUID.randomUUID().toString();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Date insertDate) {
        this.insertDate = insertDate;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Boolean getExists() {
        return isExists;
    }

    public void setExists(Boolean exists) {
        isExists = exists;
    }
}
