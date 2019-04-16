package com.yangyuan.wififileshareNio.Utils;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by yangy on 2018/1/9.
 */

public class StringSDUtil {
    public static void save(String string,String fileName){
        String filePath= Environment.getExternalStorageDirectory().getPath()+"/WifiSharingSaveDir/"+fileName;
        try {
            FileUtil.CreateDirAndFile(filePath);
            FileOutputStream writerStream = new FileOutputStream(filePath);
            BufferedWriter oWriter = new BufferedWriter(new OutputStreamWriter(writerStream, "UTF-8"));
            oWriter.write(string);
            oWriter.flush();
            oWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
