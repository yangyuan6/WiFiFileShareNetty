package com.yangyuan.wififileshareNio.Utils;

import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by yangy on 2017/3/18.
 */

public class FileInfoRemoveNoExistUtil {
    public static void DoAction(){
        ArrayList<ServiceFileInfo> serviceFileInfos=GetServiceFileInfosFromSdUtil.doAction();
        for (int i=0;i<serviceFileInfos.size();i++){
            try {
                if (serviceFileInfos.get(i).getFileType()!= FileType.app){
                    File file=new File(serviceFileInfos.get(i).getFilepath());
                    if (file==null||!file.exists()){
                        serviceFileInfos.remove(i);
                    }
                }
            } catch (Exception e) {
                serviceFileInfos.remove(i);
            }
        }
        SaveFileInfo2SdUtil.save(serviceFileInfos);
    }
}
