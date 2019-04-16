package com.yangyuan.wififileshareNio.Utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by yangy on 2018/1/5.
 */

public class DowningFileInfoUtil {
    public static void save(HashMap<String,ArrayList<ServiceFileInfo>> map){
        SharedPreferencesUtil.saveData("DowningFileInfo",GsonUtils.toJson(map));
    }
    public static HashMap<String,ArrayList<ServiceFileInfo>> get(){
        Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
        Type type = new TypeToken<HashMap<String, ArrayList<ServiceFileInfo>>>() {}.getType();
        String jsonString=(String)SharedPreferencesUtil.getData("DowningFileInfo","");
        if(TextUtils.isEmpty(jsonString)){
            return new HashMap<String,ArrayList<ServiceFileInfo>>();
        }
        HashMap<String,  ArrayList<ServiceFileInfo>> map= gson.fromJson(jsonString, type);
        return map;
    }
}
