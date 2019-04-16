package com.yangyuan.wififileshareNio.wifUtils;

import android.content.Context;
import android.net.ConnectivityManager;

import com.yangyuan.wififileshareNio.BaseApplication;

import java.lang.reflect.Method;

public class MobileDataHelper
{
	/**
	 * 设置手机的移动数据
	 */
	public static void setMobileData(boolean pBoolean) {

		try {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Class[] argsClass = new Class[1];
			argsClass[0] = boolean.class;

			Method method = ownerClass.getMethod("setMobileDataEnabled", argsClass);

			method.invoke(mConnectivityManager, pBoolean);

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("移动数据设置错误: " + e.toString());
		}
	}
	public static void toggleMobileData( boolean enabled){
		ConnectivityManager connectivityManager =
				(ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
		Method setMobileDataEnabl;
		try {
			setMobileDataEnabl = connectivityManager.getClass().getDeclaredMethod("setMobileDataEnabled", boolean.class);
			setMobileDataEnabl.invoke(connectivityManager, enabled);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 返回手机移动数据的状态
	 * @return true 连接 false 未连接
	 */
	public static boolean getMobileDataState() {

		try {

			ConnectivityManager mConnectivityManager = (ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

			Class ownerClass = mConnectivityManager.getClass();

			Method method = ownerClass.getMethod("getMobileDataEnabled");

			Boolean isOpen = (Boolean) method.invoke(mConnectivityManager);

			return isOpen;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("得到移动数据状态出错");
			return false;
		}

	}
}
