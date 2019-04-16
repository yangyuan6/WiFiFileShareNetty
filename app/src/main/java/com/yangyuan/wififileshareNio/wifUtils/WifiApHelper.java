package com.yangyuan.wififileshareNio.wifUtils;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.Utils.LogUtil;
import com.yangyuan.wififileshareNio.reciver.ApStateBroadcastReciver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by Administrator on 2014/11/25.
 */
public class WifiApHelper
{
	private WifiManager wifiManager = null;
	private WifiHelper wifihelper = null;
	private ApClosedCheckTimer closedCheckTimer = new ApClosedCheckTimer();
	private ApOpenedCheckTimer openedCheckTimer = new ApOpenedCheckTimer();

	public WifiApHelper()
	{
		wifiManager = (WifiManager) BaseApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		wifihelper = new WifiHelper();
	}

	public WifiApHelper(WifiHelper wifihelper)
	{
		wifiManager = (WifiManager) BaseApplication.getInstance().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
		this.wifihelper = wifihelper;
	}
	public WifiApHelper(WifiHelper wifihelper,Context context)
	{
		wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		this.wifihelper = wifihelper;
	}

	protected WifiConfiguration createWifiInfo(String SSID)
	{
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = SSID;


		WifiConfiguration tempConfig = this.isExsits(SSID);
		if (tempConfig != null)
		{
			wifiManager.removeNetwork(tempConfig.networkId);
		}

		config.wepKeys[0] = "";
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.wepTxKeyIndex = 0;
		return config;
	}

	protected WifiConfiguration isExsits(String SSID)
	{
		List<WifiConfiguration> lc = wifiManager.getConfiguredNetworks();
		if (lc != null)
			for (WifiConfiguration w : lc)
			{
				if (w.SSID.equals(SSID))
					return w;
			}
		return null;
	}

	/**
	 * 打开名称为SSID的wifi
	 *
	 * @param SSID       要建立的ap的名称
	 * @param needReturn 是否需要返回WifiConfiguration
	 * @return
	 */
	public WifiConfiguration openWifiAp(String SSID, boolean needReturn)
	{

		LogUtil.i(this, "openWifiAp");
		ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_OPENING);
		try
		{
			Method method=wifiManager.getClass().getDeclaredMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
			WifiConfiguration wifiConfiguration=createWifiInfo(SSID);
			/*WifiConfiguration apConfiguration = new WifiConfiguration();
			apConfiguration.SSID = SSID;
			apConfiguration.preSharedKey = WifiAdmin.PassWord;
			apConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);*/
			method.invoke(wifiManager, wifiConfiguration, true);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		if (!openedCheckTimer.isRuning())
		{
			if(openedCheckTimer.isRuned())
				openedCheckTimer = new ApOpenedCheckTimer();
			openedCheckTimer.start(20, 500);
		}
		if (needReturn)
		{
			try
			{
				Method method = wifiManager.getClass().getDeclaredMethod("getWifiApConfiguration");
				method.setAccessible(true);
				return (WifiConfiguration) method.invoke(wifiManager);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		return null;
	}

	public void startWifiAp(String ApName,String password) {
		Method method1 = null;
		try {
			method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, boolean.class);
			WifiConfiguration netConfig = new WifiConfiguration();

			netConfig.SSID = ApName;
			netConfig.preSharedKey = password;

			netConfig.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			netConfig.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			netConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			netConfig.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			netConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
			netConfig.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);

			method1.invoke(wifiManager, netConfig, true);

		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 关闭热点
	 *
	 * @param config 关闭热点时重新写入的WIFI配置
	 */
	public void closeWifiAp(WifiConfiguration config)
	{
		LogUtil.i(this, "closeWifiAp");
		if (isApEnabled())
		{
			ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_CLOSING);
			try
			{
				if (config == null)
				{
					Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
					method.setAccessible(true);
					config = (WifiConfiguration) method.invoke(wifiManager);
				}

				Method method2 = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, boolean.class);
				method2.invoke(wifiManager, config, false);
			} catch (Exception e)
			{
				e.printStackTrace();
			}
			if (!closedCheckTimer.isRuning())
			{
				if (closedCheckTimer.isRuned())
					closedCheckTimer = new ApClosedCheckTimer();
				closedCheckTimer.start(20, 500);
			}

		}
	}

	public boolean isApEnabled()
	{
		try
		{
			Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
			method.setAccessible(true);
			return (Boolean) method.invoke(wifiManager);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}


	private class ApClosedCheckTimer extends TimerCheck
	{
		@Override
		public void doTimerCheckWork()
		{
			if (!isApEnabled())
			{
				ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_CLOSED);
				exit();
			}
		}

		@Override
		public void doTimeOutWork()
		{
			ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_Operation_TimeOut);
			exit();
		}
	}

	private class ApOpenedCheckTimer extends TimerCheck
	{
		@Override
		public void doTimerCheckWork()
		{
			if (isApEnabled())
			{
				ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_OPENED);
				exit();
			}
		}

		@Override
		public void doTimeOutWork()
		{
			ApStateBroadcastReciver.sendBroadcast(ApStateBroadcastReciver.WIFI_AP_Operation_TimeOut);
			exit();
		}
	}

}
