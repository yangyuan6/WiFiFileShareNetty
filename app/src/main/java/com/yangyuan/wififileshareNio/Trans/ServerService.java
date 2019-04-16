package com.yangyuan.wififileshareNio.Trans;

import android.app.Service;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.yangyuan.wififileshareNio.Utils.ApNameUtil;
import com.yangyuan.wififileshareNio.Utils.ThreadPool;
import com.yangyuan.wififileshareNio.bean.ApNameInfo;
import com.yangyuan.wififileshareNio.nioTransfer.NioService;
import com.yangyuan.wififileshareNio.reciver.ApStateBroadcastReciver;
import com.yangyuan.wififileshareNio.reciver.WifiStateBroadcastReciver;
import com.yangyuan.wififileshareNio.sendReciver.RecvingPrepareStateChangReciver;
import com.yangyuan.wififileshareNio.wifUtils.MobileDataHelper;
import com.yangyuan.wififileshareNio.wifUtils.WifiApHelper;
import com.yangyuan.wififileshareNio.wifUtils.WifiHelper;

/**
 * Created by yangy on 2017/3/1.
 */

public class ServerService extends Service implements WifiStateBroadcastReciver.OnWIfiStateChangedListener, ApStateBroadcastReciver.OnApStateChangListener
{
    private ReceiveActionBinder binder = null;
    private final static int POOL_SIZE = 4;
    private WifiApHelper apHelper;
    private WifiHelper wifiHelper;
    private ApStateBroadcastReciver apStateBroadcastReciver;
    private WifiStateBroadcastReciver wifiStateBroadcastReciver;
    private String ssid;
    private WifiConfiguration apConfig = null;
    private ThreadPool threadPool;
    @Override
    public void onCreate()
    {
        super.onCreate();
        RecvingPrepareStateChangReciver.sendBroadcast(RecvingPrepareStateChangReciver.STATE_FINISH);
        wifiHelper = new WifiHelper();
        apHelper = new WifiApHelper(wifiHelper);
        binder = new ReceiveActionBinder();
        apStateBroadcastReciver = new ApStateBroadcastReciver();
        wifiStateBroadcastReciver = new WifiStateBroadcastReciver();

    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public void onWifiStateChanged(int wifiSatate)
    {
        if (wifiSatate == WifiManager.WIFI_STATE_DISABLED)
        {
            wifiStateBroadcastReciver.unRegisterSelf();

            WifiConfiguration config = apHelper.openWifiAp(ssid, apConfig == null);
            if(config != null)
                apConfig = config;
        }
    }

    @Override
    public void onApClosing()
    {

    }

    @Override
    public void onApOpening()
    {

    }

    @Override
    public void onApOpened()
    {
        RecvingPrepareStateChangReciver.sendBroadcast(RecvingPrepareStateChangReciver.STATE_FINISH);
        apStateBroadcastReciver.setApStateChangListener(null);
        apStateBroadcastReciver.unRegisterSelf();
/*        threadPool=new ThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);
        threadPool.execute(new StartServiceSocket(threadPool));*/

/*        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(5);
        fixedThreadPool.execute(new StartServiceSocket(fixedThreadPool));*/

        NioService nioService=new NioService();
        try {
            nioService.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onApClosed()
    {
        wifiStateBroadcastReciver.unRegisterSelf();
        WifiConfiguration config = apHelper.openWifiAp(ssid, apConfig == null);
        if(config != null)
            apConfig = config;
    }

    public class ReceiveActionBinder extends Binder
    {
        public void prepareRecive(int photoId, String name)
        {
            RecvingPrepareStateChangReciver.sendBroadcast(RecvingPrepareStateChangReciver.STATE_PREPARING);
            ssid = ApNameUtil.encodeApName(new ApNameInfo(name, photoId));
            apStateBroadcastReciver.setApStateChangListener(ServerService.this);
            apStateBroadcastReciver.registerSelf();
            if (MobileDataHelper.getMobileDataState())
            {
               /* MobileDataHelper.setMobileData(false);*/
                MobileDataHelper.toggleMobileData(false);
            }
            if ((!apHelper.isApEnabled()) && (!wifiHelper.isWifiEnabled()))
            {
                WifiConfiguration config = apHelper.openWifiAp(ssid, apConfig == null);
                if(config != null)
                    apConfig = config;
                return;
            }

            wifiStateBroadcastReciver.setOnWIfiStateChangedListener(ServerService.this);
            wifiStateBroadcastReciver.registerSelf();
            apHelper.closeWifiAp(apConfig);
            wifiHelper.setWifiEnabled(false);
        }

        public void onlyCloseAP()
        {
            threadPool.closed();
            apHelper.closeWifiAp(apConfig);
        }
        public void stopReceiveService()
        {
            threadPool.closed();
        }
    }
}
