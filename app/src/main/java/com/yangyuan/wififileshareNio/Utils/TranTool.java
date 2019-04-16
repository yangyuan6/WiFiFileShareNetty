package com.yangyuan.wififileshareNio.Utils;

import java.io.File;

/**
 * Created by yangy on 2017/3/2.
 */

public class TranTool {
    //通过文件路径得到文件名
    public static String getFileNameByPath(String path)
    {
        //File.separatorChar表示文件明文件路径区分符，比如在中英文下就是"\"，日文下"￥",Unix下"/"，
        int index = path.lastIndexOf(File.separatorChar);
        return path.substring(index+1);
    }
}
