package com.yangyuan.wififileshareNio.UI;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yangyuan.wififileshareNio.Base.BaseActivity;
import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.UI.UIUtils.CircleImageView;
import com.yangyuan.wififileshareNio.config.AppConfig;

/**
 * Created by yangy on 2017/3/1.
 */

public class SettingUserNamePhotoActivity extends BaseActivity implements View.OnClickListener {
    private int tempPhotoId = 0;
    private CircleImageView photo;
    private EditText name;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        tempPhotoId = AppConfig.photoId;
        setContentView(R.layout.setting_username_photo);
        findViewById(R.id.iv1).setOnClickListener(this);
        findViewById(R.id.iv2).setOnClickListener(this);
        findViewById(R.id.iv3).setOnClickListener(this);
        findViewById(R.id.iv4).setOnClickListener(this);
        findViewById(R.id.iv5).setOnClickListener(this);
        findViewById(R.id.iv6).setOnClickListener(this);
        findViewById(R.id.iv7).setOnClickListener(this);
        findViewById(R.id.iv8).setOnClickListener(this);

        findViewById(R.id.iv_back).setOnClickListener(this);
        ((TextView)findViewById(R.id.tv_title)).setText(R.string.setting);

        ((TextView)findViewById(R.id.btn1)).setText(R.string.cancle);
        ((TextView)findViewById(R.id.btn2)).setText(R.string.save);

        findViewById(R.id.btn1).setOnClickListener(this);
        findViewById(R.id.btn2).setOnClickListener(this);

        photo= (CircleImageView) findViewById(R.id.Photo);
        name = (EditText) findViewById(R.id.name);

        photo.setImageResource(AppConfig.getPhotoResorce());
        name.setText(AppConfig.userName);
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.iv_back:
            case R.id.btn1:
                finish();
                break;
            case R.id.iv1:
                photo.setImageResource(R.mipmap.head_portrait_1);
                tempPhotoId = 0;
                break;
            case R.id.iv2:
                photo.setImageResource(R.mipmap.head_portrait_2);
                tempPhotoId = 1;
                break;
            case R.id.iv3:
                photo.setImageResource(R.mipmap.head_portrait_3);
                tempPhotoId = 2;
                break;
            case R.id.iv4:
                photo.setImageResource(R.mipmap.head_portrait_4);
                tempPhotoId = 3;
                break;
            case R.id.iv5:
                photo.setImageResource(R.mipmap.head_portrait_5);
                tempPhotoId = 4;
                break;
            case R.id.iv6:
                photo.setImageResource(R.mipmap.head_portrait_6);
                tempPhotoId = 5;
                break;
            case R.id.iv7:
                photo.setImageResource(R.mipmap.head_portrait_7);
                tempPhotoId = 6;
                break;
            case R.id.iv8:
                photo.setImageResource(R.mipmap.head_portrait_8);
                tempPhotoId = 7;
                break;
            case R.id.btn2:
                if(name.getText().toString().isEmpty())
                {
                    BaseApplication.showToast(getString(R.string.nameIgnoreWaring));
                    return;
                }
                AppConfig.photoId = tempPhotoId;
                AppConfig.userName = name.getText().toString();
                SharedPreferences.Editor edit = getSharedPreferences(AppConfig.settingName, MODE_PRIVATE).edit();
                edit.putInt("photoId", AppConfig.photoId);
                edit.putString("name", AppConfig.userName);
                edit.commit();
                this.finish();
        }
    }
}
