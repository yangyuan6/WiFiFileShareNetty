package com.yangyuan.wififileshareNio.bean;

import java.io.Serializable;

/**
 * Created by yangy on 2017/3/1.
 */
//发送的状态
public enum SendStatus implements Serializable
{
    SenddingBegin,
    PercentChange,
    Finish,
    Cancle,
    AllFinish,
    Error;
}