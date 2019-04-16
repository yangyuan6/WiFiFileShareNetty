package com.yangyuan.wififileshareNio.reciver;

import android.content.Context;
import android.content.Intent;

import com.yangyuan.wififileshareNio.Base.BaseReciver;
import com.yangyuan.wififileshareNio.BaseApplication;

/**
 * Created by yangy on 2017/3/4.
 */

public class GetShareFileInfosReciver extends BaseReciver {
    private  OnGetShareFileInfosListener onGetShareFileInfosListener;
    private static final String INTENT_FILTER = GetShareFileInfosReciver.class.getName();
    public GetShareFileInfosReciver(){super(INTENT_FILTER);}
    @Override
    public void onReceive(Context context, Intent intent) {
        if (INTENT_FILTER.equals(intent.getAction())){
            String shareFileInfosJson=intent.getStringExtra("shareFileInfos");
            onGetShareFileInfosListener.onGetShareFileInfos(shareFileInfosJson);
        }
    }
    public void setOnGetShareFileInfosListener(OnGetShareFileInfosListener onGetShareFileInfosListener){
        this.onGetShareFileInfosListener=onGetShareFileInfosListener;
    }
    public static void sendGetShareFileInfosBroadcast(String shareFileInfos){
        Intent intent = new Intent(INTENT_FILTER);
        intent.putExtra("shareFileInfos",shareFileInfos);
        BaseApplication.getInstance().sendBroadcast(intent);
    }
    public interface OnGetShareFileInfosListener{
        void onGetShareFileInfos(String shareFileInfosJson);
    }
}
