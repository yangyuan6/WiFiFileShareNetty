package com.yangyuan.wififileshareNio.reciver;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;

import com.yangyuan.wififileshareNio.Base.BaseReciver;


/***
 * WIFI状态改变时系统发出此广播，wiif状态包括以下几种
 * WIFI_STATE_DISABLING
 * WIFI_STATE_DISABLED
 * WIFI_STATE_ENABLING
 * WIFI_STATE_ENABLED
 * WIFI_STATE_UNKNOWN
 */
public class WifiStateBroadcastReciver extends BaseReciver
{
	private OnWIfiStateChangedListener onWIfiStateChangedListener;

	public WifiStateBroadcastReciver()
	{
		super(WifiManager.WIFI_STATE_CHANGED_ACTION);
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction()))
		{
			if(onWIfiStateChangedListener != null)
			{
				int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
				onWIfiStateChangedListener.onWifiStateChanged(wifiState);
			}
		}
	}


	public void setOnWIfiStateChangedListener(OnWIfiStateChangedListener onWIfiStateChangedListener)
	{
		this.onWIfiStateChangedListener = onWIfiStateChangedListener;
	}

	public interface OnWIfiStateChangedListener
	{
		void onWifiStateChanged(int wifiSatate);
	}
}
