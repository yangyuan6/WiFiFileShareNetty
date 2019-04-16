package com.yangyuan.wififileshareNio.reciver;


import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.yangyuan.wififileshareNio.Base.BaseReciver;
import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.Utils.LogUtil;


/**
 * 系统网络的连接状态发生改变时激发此Reciver
 */
public class ConnectivityChangeReciver extends BaseReciver
{
	ConnectivityManager manager;
	OnWifiConnectivityChangeListener onWifiConnectivityChangeListener;
	OnMobileConnectivityChangeListener onMobileConnectivityChangeListener;

	public ConnectivityChangeReciver()
	{
		super(ConnectivityManager.CONNECTIVITY_ACTION);
		manager = (ConnectivityManager) BaseApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction()))
		{
			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			LogUtil.i(this, "networkInfo" + networkInfo);
			if(networkInfo == null)
				return;
			if (networkInfo.getType() ==ConnectivityManager.TYPE_WIFI  && onWifiConnectivityChangeListener != null)
			{
				onWifiConnectivityChangeListener.onWifiConnectivityChange(networkInfo);
				return;
			}
			if (networkInfo.getType() ==ConnectivityManager.TYPE_MOBILE  && onMobileConnectivityChangeListener != null)
			{
				onMobileConnectivityChangeListener.onMobileConnectivityChange(networkInfo);
				return;
			}

		}
	}

	public void setOnWifiConnectivityChangeListener(OnWifiConnectivityChangeListener onWifiConnectivityChangeListener)
	{
		this.onWifiConnectivityChangeListener = onWifiConnectivityChangeListener;
	}

	public void setOnMobileConnectivityChangeListener(OnMobileConnectivityChangeListener onMobileConnectivityChangeListener)
	{
		this.onMobileConnectivityChangeListener = onMobileConnectivityChangeListener;
	}

	public interface OnWifiConnectivityChangeListener
	{
		void onWifiConnectivityChange(NetworkInfo info);
	}

	public interface OnMobileConnectivityChangeListener
	{
		void onMobileConnectivityChange(NetworkInfo info);
	}
}
