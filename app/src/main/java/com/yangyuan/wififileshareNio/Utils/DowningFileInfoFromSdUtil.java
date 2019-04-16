package com.yangyuan.wififileshareNio.Utils;

import android.os.Environment;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yangy on 2017/3/4.
 */

public class DowningFileInfoFromSdUtil {
    public static HashMap<String,ArrayList<ServiceFileInfo>> get(){
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type = new TypeToken<HashMap<String, ArrayList<ServiceFileInfo>>>() {}.getType();
        String filePath= Environment.getExternalStorageDirectory().getPath()+"/WifiSharingSaveDir/downingFileInfo.db";
        try {
            FileUtil.CreateDirAndFile(filePath);
            File file=new File(filePath);
            BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
            String jsonString =bufferedReader.readLine();
            if(TextUtils.isEmpty(jsonString)){
                return new HashMap<String,ArrayList<ServiceFileInfo>>();
            }else{
                HashMap<String,  ArrayList<ServiceFileInfo>> map= gson.fromJson(jsonString, type);
                return map;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new HashMap<String,ArrayList<ServiceFileInfo>>();
    }
    public static void save(HashMap<String,ArrayList<ServiceFileInfo>> map){
        Gson gson=new Gson();
        String filePath= Environment.getExternalStorageDirectory().getPath()+"/WifiSharingSaveDir/downingFileInfo.db";
        try {
            FileUtil.CreateDirAndFile(filePath);
            FileOutputStream writerStream = new FileOutputStream(filePath);
            BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));
            oWriter.write(gson.toJson(map));
            oWriter.flush();
            oWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
