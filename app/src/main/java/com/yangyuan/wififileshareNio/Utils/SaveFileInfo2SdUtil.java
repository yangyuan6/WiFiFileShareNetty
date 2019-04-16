package com.yangyuan.wififileshareNio.Utils;

import android.os.Environment;

import com.google.gson.Gson;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by yangy on 2017/3/5.
 */

public class SaveFileInfo2SdUtil {
    public static void save(ArrayList<ServiceFileInfo> serviceFileInfos) {
        Gson gson = new Gson();
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/shareServiceFileInfo.db";
        try {
            FileUtil.CreateDirAndFile(filePath);
            FileOutputStream writerStream = new FileOutputStream(filePath);
            BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));
            oWriter.write(gson.toJson(serviceFileInfos));
            oWriter.flush();
            oWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String get(String fileName) {
        ArrayList<ServiceFileInfo> serviceFileInfos = new ArrayList<>();
        String str = "";
        String filePath = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/"+fileName;
        try {
            FileUtil.CreateDirAndFile(filePath);
            File file = new File(filePath);
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            str = bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str;
    }
}
