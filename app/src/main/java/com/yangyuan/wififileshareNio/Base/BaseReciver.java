package com.yangyuan.wififileshareNio.Base;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;

import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.Utils.LogUtil;


/**
 * Created by yangy on 2017/3/1.
 */

public abstract class BaseReciver extends BroadcastReceiver
{
    protected String INTENT_FILTER;
    private boolean hasRegister = false;

    public BaseReciver(final String INTENT_FILTER)
    {
        this.INTENT_FILTER = INTENT_FILTER;
    }

    public synchronized void registerSelf()
    {
        if (!hasRegister)
        {
            BaseApplication.getInstance().registerReceiver(this, new IntentFilter(INTENT_FILTER));
            LogUtil.i(this, "registerReceiver");
        }
        hasRegister = true;
    }

    public synchronized void unRegisterSelf()
    {
        if (hasRegister)
        {
            LogUtil.i(this, "unRegisterReceiver");
            BaseApplication.getInstance().unregisterReceiver(this);
        }

        hasRegister = false;
    }

    @Override
    protected void finalize() throws Throwable
    {
        unRegisterSelf();
        super.finalize();
    }
}

