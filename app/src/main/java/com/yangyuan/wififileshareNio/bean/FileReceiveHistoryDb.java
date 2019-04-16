package com.yangyuan.wififileshareNio.bean;

import org.litepal.crud.DataSupport;
import java.util.Date;
/**
 * Created by yangy on 2017/3/8.
 */

public class FileReceiveHistoryDb extends DataSupport{
    private int id;
    private Date insertDate;
    private String path;
    private String desc;
    private String fileType;
    private long size;
    public boolean isExists;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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


}
