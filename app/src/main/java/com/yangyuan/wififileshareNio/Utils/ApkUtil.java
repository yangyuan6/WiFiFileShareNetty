package com.yangyuan.wififileshareNio.Utils;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.R;

public class ApkUtil
{
	//得到app的图片
	public static Drawable getAPPDrawable(String path)
	{
		PackageManager pm = BaseApplication.getInstance().getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if (info != null)
		{
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.publicSourceDir = path;
			Drawable icon = pm.getApplicationIcon(appInfo);
			if(icon != null)
				return icon;
		}

		return BaseApplication.getInstance().getResources().getDrawable(R.mipmap.apk);
	}
	//得到app名称
	public static String getAPPName(String path)
	{
		PackageManager pm = BaseApplication.getInstance().getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
		if (info != null)
		{
			ApplicationInfo appInfo = info.applicationInfo;
			appInfo.publicSourceDir = path;
			String name = pm.getApplicationLabel(appInfo).toString();
			if(!TextUtils.isEmpty(name))
				return name;
		}

		return null;
	}

}
