package com.yangyuan.wififileshareNio.UI;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yangyuan.wififileshareNio.Base.BaseActivity;
import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.UI.UIUtils.FileChoseTab;
import com.yangyuan.wififileshareNio.Utils.FileInfoRemoveNoExistUtil;
import com.yangyuan.wififileshareNio.Utils.FileUtil;
import com.yangyuan.wififileshareNio.Utils.GetPhoneIdUtil;
import com.yangyuan.wififileshareNio.Utils.GetServiceFileInfosFromSdUtil;
import com.yangyuan.wififileshareNio.Utils.LogUtil;
import com.yangyuan.wififileshareNio.Utils.SaveFileInfo2SdUtil;
import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.Range;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.choser.AppFileChoser;
import com.yangyuan.wififileshareNio.choser.GetChoseFile;
import com.yangyuan.wififileshareNio.choser.ImageFileChoser;
import com.yangyuan.wififileshareNio.choser.MusicFileChoser;
import com.yangyuan.wififileshareNio.choser.VideoFileChoser;
import com.yangyuan.wififileshareNio.page.TabPageIndicator;
import com.yangyuan.wififileshareNio.page.TabTitleAdapter;
import com.yangyuan.wififileshareNio.page.TabView;
import com.yangyuan.wififileshareNio.sendReciver.FileChoseChangedReciver;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import zhou.tools.fileselector.FileSelector;
import zhou.tools.fileselector.FileSelectorActivity;
import zhou.tools.fileselector.config.FileConfig;

/**
 * Created by yangy on 2017/3/2.
 */

public class FileChoseActivity extends BaseActivity implements FileChoseChangedReciver.OnFileChoseChangedListener, View.OnClickListener {

    private TabPageIndicator indicator;
    private ViewPager viewPager;
    private TextView btn1;
    private TextView btn2;
    private int choseFile = 0;
    private ImageView ivBack;
    private TextView tvTitle;
    private TextView btn_look_sharefileinfo;
    FileChoseChangedReciver fileChoseChanged;
    private FileConfig fileConfig;
    private ArrayList<ServiceFileInfo> originalServiceFileInfos;
    private int flag=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_chose);
        //新开一个线程判断分享文件是否存在,不存在就移除
        new Thread() {
            @Override
            public void run() {
                FileInfoRemoveNoExistUtil.DoAction();
            }
        }.start();

        fileConfig = new FileConfig();
        fileChoseChanged = new FileChoseChangedReciver();
        fileChoseChanged.setOnFileChoseChangedListener(this);
        fileChoseChanged.registerSelf();
        initServiceFileInfo();
        initView();
    }

    @Override
    protected void onDestroy() {
        fileChoseChanged.unRegisterSelf();
        super.onDestroy();
    }

    private void initView() {
        indicator = (TabPageIndicator) findViewById(R.id.indicator);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        btn1 = (TextView) findViewById(R.id.btn1);
        btn2 = (TextView) findViewById(R.id.btn2);
        ivBack = (ImageView) findViewById(R.id.iv_back);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        findViewById(R.id.btn_select_sharefile).setOnClickListener(this);
        btn_look_sharefileinfo = (TextView) findViewById(R.id.btn_look_sharefileinfo);
        btn_look_sharefileinfo.setOnClickListener(this);
        tvTitle.setText(R.string.pleaseChoseFile);
        ivBack.setOnClickListener(this);
        btn1.setText(String.format(getString(R.string.hasChosed), choseFile));
        btn2.setText("开启分享");
        btn2.setOnClickListener(this);
        viewPager.setAdapter(new FileChoseAdapter(getSupportFragmentManager()));
        indicator.setViewPager(viewPager);
    }

    @Override
    public void onFileChoseChang(boolean increse) {
        if (increse) choseFile++;
        else choseFile--;
        /*btn1.setEnabled(choseFile > 0);
        btn2.setEnabled(choseFile > 0);*/

        btn1.setText(String.format(getString(R.string.hasChosed), choseFile));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn2:
                ArrayList<ServiceFileInfo> tempServiceFileInfos = getAllSendFileInfo();
                if (tempServiceFileInfos == null || tempServiceFileInfos.size() <= 0) {
                    if (originalServiceFileInfos.size() <= 0) {
                        originalServiceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
                        if(flag==0){
                            flag=1;
                            return;
                        }
                        Toast.makeText(this, "请先选择要分享的文件", Toast.LENGTH_SHORT).show();
                        originalServiceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
                    } else {
                        Intent intent = new Intent(this, ShareActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("ShareFileList", originalServiceFileInfos);
                        intent.putExtras(bundle);
                        startActivity(intent);
                    }
                } else {
                    saveFileInfo(getAllSendFileInfo());
                    Intent intent = new Intent(this, ShareActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("ShareFileList", originalServiceFileInfos);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }

                break;
            case R.id.iv_back:
                this.finish();
                break;
            case R.id.btn_look_sharefileinfo: {
                Intent intent = new Intent(this, ManageShareFileActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.btn_select_sharefile: {
                Intent intent = new Intent(getApplicationContext(), FileSelectorActivity.class);
                fileConfig.startPath = Environment.getExternalStorageDirectory().getPath();

                fileConfig.rootPath = "/";
                intent.putExtra(FileConfig.FILE_CONFIG, fileConfig);
                startActivityForResult(intent, 0);
                break;
            }
        }
    }

    private ArrayList<ServiceFileInfo> getAllSendFileInfo() {
        ArrayList<ServiceFileInfo> sendFileInfos = new ArrayList<>(30);
        Fragment[] fragments = ((FileChoseAdapter) viewPager.getAdapter()).getFragments();
        for (int i = 0; i < fragments.length; i++) {
            Collection<ServiceFileInfo> info = ((GetChoseFile) fragments[i]).getChosedFile();
            if (info != null)
                sendFileInfos.addAll(info);
        }
        return sendFileInfos;
    }

    class FileChoseAdapter extends FragmentPagerAdapter implements TabTitleAdapter {
        private String[] titles = new String[]{"视频", "音乐", "应用", "图片"};

        Fragment[] getFragments() {
            return fragments;
        }

        Fragment[] fragments = new Fragment[4];

        public FileChoseAdapter(FragmentManager fm) {
            super(fm);
            fragments[0] = new VideoFileChoser();
            fragments[1] = new MusicFileChoser();
            fragments[2] = new AppFileChoser();
            fragments[3] = new ImageFileChoser();

        }

        @Override
        public Fragment getItem(int index) {
            LogUtil.d(this, index + "");
            return fragments[index];
        }

        @Override
        public TabView getTabView(int index) {
            FileChoseTab tab = new FileChoseTab(FileChoseActivity.this, index);
            tab.setTabText(titles[index]);
            return tab;
        }

        @Override
        public int getCount() {
            return titles.length;
        }
    }

    private void saveFileInfo(ArrayList<ServiceFileInfo> serviceFileInfos) {
        int tag = 0;
        for (ServiceFileInfo serviceFileInfo : serviceFileInfos) {
            tag = 0;
            for (ServiceFileInfo originalServiceFileInfo : originalServiceFileInfos) {
                if (originalServiceFileInfo.getFilepath().equals(serviceFileInfo.getFilepath())) {
                    tag = 1;
                    break;
                }
            }
            if (tag == 0) {
                originalServiceFileInfos.add(serviceFileInfo);
            } else {
                tag = 0;
            }

        }
        String phoneId = GetPhoneIdUtil.doAction(this);
        for (int i = 0; i < originalServiceFileInfos.size(); i++) {
            if (originalServiceFileInfos.get(i).getFileType().equals(FileType.app)) {
                originalServiceFileInfos.get(i).setFileName(originalServiceFileInfos.get(i).getFileDesc());
            } else {
                File file = new File(originalServiceFileInfos.get(i).getFilepath());
                originalServiceFileInfos.get(i).setFileName(file.getName());
            }
            originalServiceFileInfos.get(i).setPhoneId(phoneId);
        }

        SaveFileInfo2SdUtil.save(originalServiceFileInfos);
    }

    private void initServiceFileInfo() {
        this.originalServiceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> list = data.getStringArrayListExtra(FileSelector.RESULT);
                int tag = 0;
                for (String filePath : list) {
                    tag = 0;
                    for (ServiceFileInfo originalServiceFileInfo : originalServiceFileInfos) {
                        if (originalServiceFileInfo.getFilepath().equals(filePath)) {
                            tag = 1;
                            break;
                        }
                    }
                    if (tag == 0) {
                        File file = new File(filePath);
                        ServiceFileInfo serviceFileInfo = new ServiceFileInfo();
                        serviceFileInfo.setFileDesc(filePath);
                        serviceFileInfo.setFilepath(filePath);
                        serviceFileInfo.setFileName(file.getName());
                        String suffix = FileUtil.getType(filePath);
                        serviceFileInfo.setFileType(FileType.parse(suffix));
                        serviceFileInfo.setTransRange(Range.getByPath(filePath));
                        originalServiceFileInfos.add(serviceFileInfo);
                    } else {
                        tag = 0;
                    }
                    SaveFileInfo2SdUtil.save(originalServiceFileInfos);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
