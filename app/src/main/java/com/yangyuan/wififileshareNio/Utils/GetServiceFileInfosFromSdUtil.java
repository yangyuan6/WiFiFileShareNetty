package com.yangyuan.wififileshareNio.Utils;

import android.os.Environment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangy on 2017/3/4.
 */

public class GetServiceFileInfosFromSdUtil {
    public static ArrayList<ServiceFileInfo> doAction(){
        ArrayList<ServiceFileInfo> serviceFileInfos=new ArrayList<>();
        String filePath= Environment.getExternalStorageDirectory().getPath()+"/WifiSharingSaveDir/shareServiceFileInfo.db";
        try {
            FileUtil.CreateDirAndFile(filePath);
            File file=new File(filePath);
            BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
            String str =bufferedReader.readLine();
            if(str!=null){
                Gson gson=new Gson();
                serviceFileInfos=gson.fromJson(str, new TypeToken<List<ServiceFileInfo>>() {
                }.getType());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serviceFileInfos;
    }
}
