package com.yangyuan.wififileshareNio.Trans;

import android.app.Service;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;

import com.yangyuan.wififileshareNio.Utils.ApNameUtil;
import com.yangyuan.wififileshareNio.Utils.LogUtil;
import com.yangyuan.wififileshareNio.bean.ApNameInfo;
import com.yangyuan.wififileshareNio.reciver.ApStateBroadcastReciver;
import com.yangyuan.wififileshareNio.reciver.ConnectivityChangeReciver;
import com.yangyuan.wififileshareNio.reciver.ScanResultAviableReciver;
import com.yangyuan.wififileshareNio.reciver.WifiStateBroadcastReciver;
import com.yangyuan.wififileshareNio.sendReciver.ConnectToTargetWifiReciver;
import com.yangyuan.wififileshareNio.sendReciver.ScanReciverResultReciver;
import com.yangyuan.wififileshareNio.wifUtils.MobileDataHelper;
import com.yangyuan.wififileshareNio.wifUtils.WifiApHelper;
import com.yangyuan.wififileshareNio.wifUtils.WifiHelper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yangy on 2017/3/1.
 */

public class SendService extends Service implements ScanResultAviableReciver.OnScanResultAviableListener, ConnectivityChangeReciver.OnWifiConnectivityChangeListener
{
    private SendActionBinder bind = null;
    private WifiApHelper apHelper;
    private WifiHelper wifiHelper;
    private String targetSSID;
   /* private SendTask sendTask;*/

    private ApStateBroadcastReciver apStateBroadcastReciver;
    private WifiStateBroadcastReciver wifiStateBroadcastReciver;
    private ScanResultAviableReciver scanResultAviableReciver;
    private ConnectivityChangeReciver connectivityChangeReciver;

    @Override
    public void onCreate()
    {
        super.onCreate();
        wifiHelper = new WifiHelper();
        apHelper = new WifiApHelper(wifiHelper);
        bind = new SendActionBinder();
        apStateBroadcastReciver = new ApStateBroadcastReciver();
        wifiStateBroadcastReciver = new WifiStateBroadcastReciver();
        scanResultAviableReciver = new ScanResultAviableReciver();
        connectivityChangeReciver = new ConnectivityChangeReciver();
        apStateBroadcastReciver.setApStateChangListener(new ApStateBroadcastReciver.OnApStateChangListener()
        {
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

            }

            @Override
            public void onApClosed()
            {
                apStateBroadcastReciver.unRegisterSelf();
                step2WifiOpen();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return bind;
    }

    @Override
    public void onScanResultAviable()
    {
        List<ScanResult> results = wifiHelper.getScanResult();
        ScanReciverResultReciver.sendBroadcast(usefulApFilter(results));
    }

    public ArrayList<ApNameInfo> usefulApFilter(List<ScanResult> results)
    {
        ArrayList<ApNameInfo> infos = new ArrayList<>();
        if (results == null || results.size() == 0)
            return infos;
        //去重
        HashSet<String> ssids = new HashSet<>();
        for (int i = 0; i < results.size(); i++)
        {
            ssids.add(results.get(i).SSID);
        }


        Iterator<String> iterator = ssids.iterator();
        while (iterator.hasNext())
        {
            String result = iterator.next();
            LogUtil.e(null,result);
            ApNameInfo ap = ApNameUtil.decodeApName(result);
            if (ap != null)
                infos.add(ap);
        }
        return infos;
    }

    protected void step1ApClose()
    {
        if (apHelper.isApEnabled())
        {
            apStateBroadcastReciver.registerSelf();
            apHelper.closeWifiAp(null);
        } else
        {
            step2WifiOpen();
        }
    }

    protected void step2WifiOpen()
    {
        if (wifiHelper.isWifiEnabled())
            step3WifiDisConnected();
        else
        {
            wifiStateBroadcastReciver.registerSelf();
            wifiStateBroadcastReciver.setOnWIfiStateChangedListener(new WifiStateBroadcastReciver.OnWIfiStateChangedListener()
            {

                @Override
                public void onWifiStateChanged(int wifiSatate)
                {
                    if (wifiSatate == WifiManager.WIFI_STATE_ENABLED)
                    {
                        step3WifiDisConnected();
                        wifiStateBroadcastReciver.unRegisterSelf();
                    }
                }
            });
            wifiHelper.setWifiEnabled(true);
        }
    }

    protected void step3WifiDisConnected()
    {
        step4ScanAp();
    }

    protected void step4ScanAp()
    {

        scanResultAviableReciver.setOnScanResultAviableListener(this);
        scanResultAviableReciver.registerSelf();
        wifiHelper.scanApList();
    }

    /**
     * 当WIFI的连接状态发生改变时调用
     *
     * @param info
     */
    @Override
    public void onWifiConnectivityChange(NetworkInfo info)
    {
        if (info.isConnected() && info.getState() == NetworkInfo.State.CONNECTED && info.isAvailable())
        {
            if (targetSSID.equals(wifiHelper.getCurrentConnectedSSID()) || ("\""+targetSSID+"\"").equals(wifiHelper.getCurrentConnectedSSID()))
            {
                connectivityChangeReciver.setOnWifiConnectivityChangeListener(null);
                connectivityChangeReciver.unRegisterSelf();
                ConnectToTargetWifiReciver.sendBroadcast(targetSSID);
            } else
            {
                wifiHelper.addNetwork(targetSSID, "", WifiHelper.TYPE_NO_PASSWD);
            }
        }
    }

    public class SendActionBinder extends Binder
    {
        /**
         * Activity<--->Service
         * 1:ACtivity通知Service为传输做为准备 --->
         * 2:Service通知ActivityWIFI是否已经就绪（失败，或正在打开）<---
         * 3:Service通知Activitywifi扫描已经就绪，通知Activity通过Service获取扫描列表<---
         * 4：ACtivity得到列表后选择合适的热点通知Service进行连接--->
         * 5：Service通知Activity连接客户端的状态<---
         * 6：若成功，则Service持续报告传输状态<---
         * 7：在传输期间ACtivity可以通知Service取消本次发送--->
         */
        //准备发送将移动数据关闭
        public void preparedTranSend()
        {
            if (MobileDataHelper.getMobileDataState())
                MobileDataHelper.setMobileData(false);
            step1ApClose();
        }
        //扫描热点
        public void scanAP()
        {
            wifiHelper.scanApList();
        }
        // 连接热点
        public void connectionSSID(final String ssid)
        {

            /*if(wifiHelper.isWifiContected() == WifiHelper.WIFI_CONNECTING && ssid != null)
                return;*/

            targetSSID = ssid;
            if(wifiHelper.isWifiContected()==WifiHelper.WIFI_CONNECTED){
                String contectedSsid=wifiHelper.getCurrentConnectedSSID();
                if (!ssid.equals(contectedSsid)){
                    wifiHelper.disconnectWifi(wifiHelper.getIntIp());
                    wifiHelper.addNetwork(ssid, "", WifiHelper.TYPE_NO_PASSWD);
                }
            }
//            NetworkInfo info=wifiHelper.get

            if ((wifiHelper.isWifiContected()==WifiHelper.WIFI_CONNECTED)&&targetSSID.equals(wifiHelper.getCurrentConnectedSSID())/*&&wifiHelper.pingSupplicant()*/ /*|| ("\""+targetSSID+"\"").equals(wifiHelper.getCurrentConnectedSSID())*/)
            {
                ConnectToTargetWifiReciver.sendBroadcast(targetSSID);
                return;
                //更加稳定的连接
               /* try {
                    NetworkInfo networkInfo=null;
                    while (true){
                        ConnectivityManager manager = (ConnectivityManager) BaseApplication.getInstance().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                        networkInfo = manager.getActiveNetworkInfo();
                        Thread.sleep(1000);
                        if (networkInfo!=null){
                            break;
                        }
                    }
                    if (networkInfo.isConnected() && networkInfo.getState() == NetworkInfo.State.CONNECTED && networkInfo.isAvailable()){
                        ConnectToTargetWifiReciver.sendBroadcast(targetSSID);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
            }
            connectivityChangeReciver.setOnWifiConnectivityChangeListener(SendService.this);
            connectivityChangeReciver.registerSelf();

            wifiHelper.addNetwork(ssid, "", WifiHelper.TYPE_NO_PASSWD);
        }
        public void getShareFileInfos(InetAddress address)
        {
            new ClientGetFileInfosHandler(address).start();
        }

    }

}
