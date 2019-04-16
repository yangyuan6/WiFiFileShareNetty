package com.yangyuan.wififileshareNio.Utils;

/**
 * Created by yangy on 2017/3/5.
 */

public class GetnameByPathUtil {
    public static String getName(String path){
        return path.substring(path.lastIndexOf("/")+1);
    }
}
