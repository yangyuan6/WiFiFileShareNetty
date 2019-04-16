package com.yangyuan.wififileshareNio.UI;

import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.UI.UIUtils.FlikerProgressBar;
import com.yangyuan.wififileshareNio.Utils.DowningFileInfoFromSdUtil;
import com.yangyuan.wififileshareNio.Utils.FileSizeFormatUtil;
import com.yangyuan.wififileshareNio.Utils.GetFileReceiveHistoriesUtil;
import com.yangyuan.wififileshareNio.Utils.GetServiceFileInfosFromSdUtil;
import com.yangyuan.wififileshareNio.Utils.IcoUtil;
import com.yangyuan.wififileshareNio.Utils.Ipint2StringUtil;
import com.yangyuan.wififileshareNio.Utils.SaveFileInfo2SdUtil;
import com.yangyuan.wififileshareNio.Utils.SaveFileReceiveHistories2SdUtil;
import com.yangyuan.wififileshareNio.bean.FileReceiveHistory;
import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.SendStatus;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.nioTransfer.NioGetFileClient;
import com.yangyuan.wififileshareNio.nioTransfer.NioGetHalfFileClient;
import com.yangyuan.wififileshareNio.sendReciver.SendStateChangedReciver;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


/**
 * Created by yangy on 2017/3/5.
 */

public class NioDownFilesActivity extends Activity implements View.OnClickListener, SendStateChangedReciver.OnSendStateChangedListener {
    private ArrayList<ServiceFileInfo> downFiles;
    private ListView listView;
    private DownFilesAdapter adapter;
    private TextView bottomButton;
    private SendStateChangedReciver sendStateChangedReciver = new SendStateChangedReciver();
    private final int SUCCESS = 5;
    private final int AllFINISH = 6;
    private final int ONEFINISH = 1;
    private NioGetFileClient nioGetFileClient;
    private NioGetHalfFileClient nioGetHalfFileClient;
    private ArrayList<FileReceiveHistory> fileReceiveHistories;
    private ArrayList<ServiceFileInfo> serviceFileInfos;
    private String uuid;
    private int finishIndex = -1;
    private boolean allfinishTag = false;
    private int downhalffile;

    private Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SUCCESS: {
                    String uuid = (String) msg.obj;
                    int percent = msg.arg1;
                    int receiveByteLength = msg.arg2;
                    ViewHolder holder = getViewHolderByUUID(uuid);
                    if (holder != null) {
                        int index = getIndexByUuid(uuid);
                        if (index >= 0) {
                            listView.setSelection(index);
                            downFiles.get(index).getTransRange().setBeginByte(receiveByteLength);
                        }
                        holder = getViewHolderByUUID(uuid);
                    }
                    if (holder == null)
                        return;
                    holder.data.setSendStatu(SendStatus.PercentChange);
                    holder.data.setSendPercent(percent);
                    holder.progressBar.setProgress(percent);
                    break;
                }
                case ONEFINISH: {
                    uuid = (String) msg.obj;
                    int percent =100;
                    ViewHolder holder = getViewHolderByUUID(uuid);
                    if (holder == null) {
                        finishIndex = getIndexByUuid(uuid);
                        if (finishIndex >= 0) {
                            listView.setSelection(finishIndex);
                        }
                        holder = getViewHolderByUUID(uuid);
                    }
                    if (holder == null)
                        return;
                    holder.data.setSendStatu(SendStatus.PercentChange);
                    holder.data.setSendPercent(percent);
                    holder.progressBar.setProgress(percent);
                    new Thread() {
                        @Override
                        public void run() {
                            //在子线程中去执行
                            for (int i = 0; i < downFiles.size(); i++) {
                                if (uuid.equals(downFiles.get(i).getUuid())) {
                                    ServiceFileInfo serviceFileInfo = downFiles.get(i);
                                    String downfilePath = serviceFileInfo.getFilepath();
                                    String filepath = Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/" + downfilePath.substring(downfilePath.lastIndexOf('/') + 1, downfilePath.length());
                                    FileReceiveHistory fileReceiveHistory = new FileReceiveHistory();
                                    fileReceiveHistory.setFileName(serviceFileInfo.getFileName());
                                    fileReceiveHistory.setDesc(serviceFileInfo.getFileDesc());
                                    fileReceiveHistory.setFileType(serviceFileInfo.getFileType().toString());
                                    if (serviceFileInfo.getFileType() == FileType.app) {
                                        fileReceiveHistory.setPath(Environment.getExternalStorageDirectory().getPath() + "/WifiSharingSaveDir/" + serviceFileInfo.getFileName() + ".apk");
                                    } else {
                                        fileReceiveHistory.setPath(filepath);
                                    }
                                    fileReceiveHistory.setInsertDate(new Date(System.currentTimeMillis()));
                                    fileReceiveHistory.setSize(serviceFileInfo.getTransRange().getBeginByte());
                                    fileReceiveHistory.setExists(true);
                                    int tag = 0;
                                    for (int j = 0; j < fileReceiveHistories.size(); j++) {
                                        if (fileReceiveHistories.get(j).getPath().equals(fileReceiveHistory.getPath())) {
                                            tag = 1;
                                            break;
                                        }
                                    }
                                    if (tag == 0) {
                                        fileReceiveHistories.add(fileReceiveHistory);
                                        ServiceFileInfo serviceFileInfo1 = downFiles.get(i);
                                        serviceFileInfo1.setFilepath(fileReceiveHistory.getPath());
                                        serviceFileInfos.add(serviceFileInfo1);
                                    }
                                }
                            }
                        }
                    }.start();

                    break;
                }
                case AllFINISH: {
                    allfinishTag = true;
                    SaveFileReceiveHistories2SdUtil.doAction(fileReceiveHistories);
                    SaveFileInfo2SdUtil.save(serviceFileInfos);
                    //((TextView) findViewById(R.id.tv_title)).setText("下载列表");
                    bottomButton.setText("完成");
                    break;
                }

            }
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.downingfiles);
        sendStateChangedReciver.registerSelf();
        sendStateChangedReciver.setOnSendStateChangedListener(this);
        bottomButton = (TextView) findViewById(R.id.bottomButton);
        TextView tv_title = (TextView) findViewById(R.id.tv_title);
        tv_title.setText("下载列表");
        ImageView iv_back = (ImageView) findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        bottomButton.setText("取消");
        bottomButton.setOnClickListener(this);
        downFiles = (ArrayList<ServiceFileInfo>) getIntent().getSerializableExtra("downFiles");
        downhalffile = getIntent().getIntExtra("downhalfFile", 0);
        fileReceiveHistories = GetFileReceiveHistoriesUtil.doAction();
        serviceFileInfos = GetServiceFileInfosFromSdUtil.doAction();
        initDateAndView();
        taskInit();
    }

    private void initDateAndView() {

        bottomButton = (TextView) findViewById(R.id.bottomButton);
        bottomButton.setText("取消");
        bottomButton.setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);
        listView = (ListView) findViewById(R.id.sendList);
        adapter = new DownFilesAdapter(LayoutInflater.from(this), downFiles);
        listView.setAdapter(adapter);


    }

    public void taskInit() {
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String ip = Ipint2StringUtil.int2StringIp(dhcpInfo.gateway);
        if (downhalffile ==1) {
           new Thread(){
               @Override
               public void run() {
                   nioGetHalfFileClient = new NioGetHalfFileClient();
                   try {
                       nioGetHalfFileClient.connect(ip, downFiles, uiHandler);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }
               }
           }.start();
        } else {
            new Thread() {
                @Override
                public void run() {
                    nioGetFileClient = new NioGetFileClient();
                    try {
                        nioGetFileClient.connect(ip, downFiles, uiHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }



    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                /** 应该先ShowPopWindow提示用户是否真的要退出*/
                NioDownFilesActivity.this.onDestroy();
                this.finish();
                break;
            case R.id.bottomButton: {
                if (bottomButton.getText().equals("取消")) {
                    NioDownFilesActivity.this.onDestroy();
                    this.finish();
                } else if (bottomButton.getText().equals("完成")) {
                    this.finish();
                }
                break;
            }
        }
    }

    /**
     * 当发送状态发生改变的时候响应此函数，用以更新UI
     *
     * @param uuid
     * @param state
     * @param percent
     */
    @Override
    public void onSendStateChanged(String uuid, SendStatus state, float percent) {
        if (state == SendStatus.AllFinish) {
            bottomButton.setText("完成");
            return;
        }
        ViewHolder holder = getViewHolderByUUID(uuid);
        if (holder == null) {
            int index = getIndexByUuid(uuid);
            if (index >= 0)
                listView.setSelection(index);
            holder = getViewHolderByUUID(uuid);
        }
        if (holder == null)
            return;
        holder.data.setSendStatu(state);
        holder.data.setSendPercent(percent);
        holder.progressBar.setProgress((int) (100 * percent));

    }

    /***
     * 判定给定UUID的view是否在用户可见的屏幕区域
     */
    public boolean isUUidViewVisiable(String uuid) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        int index = getIndexByUuid(uuid);
        return index >= firstListItemPosition && index <= lastListItemPosition;
    }

    //通过UUID得到对应显示发送信息的索引值
    public int getIndexByUuid(String uuid) {
        if (downFiles == null || downFiles.size() == 0)
            return -1;
        for (int i = 0; i < downFiles.size(); i++) {
            if (downFiles.get(i).getUuid().equals(uuid))
                return i;
        }
        return -1;
    }

    public ViewHolder getViewHolderByUUID(String uuid) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        int length = lastListItemPosition - firstListItemPosition + 1;
        for (int i = 0; i < length; i++) {
            View view = listView.getChildAt(i);
            if (uuid.equals(((ViewHolder) view.getTag()).data.getUuid())) {
                return (ViewHolder) view.getTag();
            }
        }
        return null;
    }

    class DownFilesAdapter extends BaseAdapter {
        private LayoutInflater inflater;
        private ArrayList<ServiceFileInfo> sendFileInfos;

        public DownFilesAdapter(LayoutInflater inflater, ArrayList<ServiceFileInfo> sendFileInfos) {
            this.inflater = inflater;
            this.sendFileInfos = sendFileInfos;
        }

        @Override
        public int getCount() {
            return sendFileInfos == null ? 0 : sendFileInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return sendFileInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.dow_file_list_item, null);
                ViewHolder holder = new ViewHolder();
                convertView.setTag(holder);
                holder.img = (ImageView) convertView.findViewById(R.id.iv_sendImg);
                holder.fileSize = (TextView) convertView.findViewById(R.id.tv_sendSize);
                holder.fileName = (TextView) convertView.findViewById(R.id.tv_sendFileName);
                holder.progressBar = (FlikerProgressBar) convertView.findViewById(R.id.pb_sendProgress);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.data = sendFileInfos.get(position);
            holder.img.setImageDrawable(IcoUtil.getIco(holder.data.getFileType(), holder.data.getFilepath()));
            holder.fileSize.setText(FileSizeFormatUtil.format(holder.data.getTransRange().getEndByte() - holder.data.getTransRange().getBeginByte()));
            holder.fileName.setText(holder.data.getFileName());
            holder.progressBar.setProgress((int) (holder.data.getSendPercent()));
            return convertView;
        }

    }

    class ViewHolder {
        public ServiceFileInfo data;
        public ImageView img;
        public TextView fileSize;
        public TextView fileName;
        public FlikerProgressBar progressBar;
    }

    @Override
    protected void onDestroy() {
        if (downFiles.size() != 0) {
            HashMap<String, ArrayList<ServiceFileInfo>> hashMap = DowningFileInfoFromSdUtil.get();
            String phoneId = downFiles.get(0).getPhoneId();
            if (!allfinishTag) {
/*                if (clientGetFileListHandler!=null){
                    clientGetFileListHandler.setCancel(true);
                }else{
                    clientGetHalfFileHandler.setCancel(true);
                }*/
                ArrayList<ServiceFileInfo> saveDownFileInfos = new ArrayList<>();
                for (int i = finishIndex + 1; i < downFiles.size(); i++) {
                    saveDownFileInfos.add(downFiles.get(i));
                }
                hashMap.put(phoneId, saveDownFileInfos);
            } else if (downhalffile == 1) {
                hashMap.remove(phoneId);
            }
            DowningFileInfoFromSdUtil.save(hashMap);
        }


        super.onDestroy();
    }
}


