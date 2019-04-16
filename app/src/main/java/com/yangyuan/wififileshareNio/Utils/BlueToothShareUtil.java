package com.yangyuan.wififileshareNio.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.UI.UIUtils.ALertUtil.Alerter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by yangy on 2017/3/1.
 */

public class BlueToothShareUtil
{
    public static void SendFileByBlueTooth(Context context, String aFileName)
    {
        ArrayList<Uri> vUriArray = new ArrayList<>();
        vUriArray.add(Uri.fromFile(new File(aFileName)));
        SendFile(context, vUriArray);
    }

    public static void SendFile(Context aContext, ArrayList<Uri> aUriArray)
    {
        if(aUriArray!=null && aUriArray.size()>0)
        {
            try {
                final String mBluetoothPackageName = "com.android.bluetooth";
                final String mBluetoothClassName = "com.android.bluetooth.opp.BluetoothOppLauncherActivity";
                Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                intent.setClassName(mBluetoothPackageName, mBluetoothClassName);
                intent.putExtra(Intent.EXTRA_STREAM, aUriArray);
                intent.setType("*/*");
                aContext.startActivity(intent);
            } catch (Exception e) {
                Alerter.create((Activity) aContext)
                        .setText("该设备不支持蓝牙分享功能")
                        .setBackgroundColor(R.color.colorAccent)
                        .setIcon(R.drawable.alerter_ic_face)
                        .show();
                e.printStackTrace();
            }
        }
    }

    // 获取当前目录下所有文件的uri
    public static ArrayList<Uri> GetTotalUris(String aFilePath, ArrayList<Uri> aUriArray)
    {
        ArrayList<Uri> vRetArray = aUriArray;
        if(vRetArray == null)
        {
            vRetArray = new ArrayList<Uri>();
        }
        // 获取源文件夹当前下的文件或目录
        File vCurFile = new File(aFilePath);
        if(vCurFile.isFile())
        {
            vRetArray.add(Uri.fromFile(vCurFile));
        }
        else
        {
            File[] files = vCurFile.listFiles();
            for (File vFile: files)
            {
                if(vFile.isFile())
                {
                    vRetArray.add(Uri.fromFile(vFile));
                }
                else if(vFile.isDirectory())
                {
                    GetTotalUris(vFile.getAbsolutePath(),vRetArray);
                }
            }
        }
        return vRetArray;
    }
}
