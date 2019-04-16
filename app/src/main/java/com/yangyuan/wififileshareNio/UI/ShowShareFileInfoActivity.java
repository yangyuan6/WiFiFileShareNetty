package com.yangyuan.wififileshareNio.UI;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.Utils.DowningFileInfoFromSdUtil;
import com.yangyuan.wififileshareNio.Utils.FileSizeFormatUtil;
import com.yangyuan.wififileshareNio.Utils.IcoUtil;
import com.yangyuan.wififileshareNio.Utils.Ipint2StringUtil;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.nioTransfer.NioGetFileInfosClient;
import com.yangyuan.wififileshareNio.reciver.GetShareFileInfosReciver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.yangyuan.wififileshareNio.R.id.iv_sendImg;
import static com.yangyuan.wififileshareNio.R.id.tv_sendFileName;
import static com.yangyuan.wififileshareNio.R.id.tv_sendSize;

/**
 * Created by yangy on 2017/3/4.
 */

public class ShowShareFileInfoActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener {
    private GetShareFileInfosReciver getShareFileInfosReciver=new GetShareFileInfosReciver();
    protected  final int GETINFOS_SUCCESS = 1;
    private final static int POOL_SIZE = 4;//单个CPU时线程池中工作线程的数目
    public static ArrayList<ServiceFileInfo> getFilesInfos;
    private ListView lv;
    private TextView btn_downfiles;
    private ArrayList<ServiceFileInfo> downFiles=new ArrayList<>();
    private boolean [] saveItemClick;
    private boolean isGetFIleInfo=false;
    private Handler getFileInfoHandler=new Handler();
    private Runnable runnable=new Runnable() {
        @Override
        public void run() {
            if (!isGetFIleInfo){
                initGetFileInfo();
                getFileInfoHandler.postDelayed(runnable,2500);
            }else{
                getFileInfoHandler.removeCallbacks(runnable);
            }
        }
    };
    private Runnable threadRemove=new Runnable() {
        @Override
        public void run() {
            try {
                getFileInfoHandler.removeCallbacks(runnable);
            } catch (Exception e) {
            }
        }
    };
    private Handler uiHandler = new Handler() {
        // 用来接收消息，处理消息的，由于handler是在主线程里面创建的。handleMessage在主线程（ui线程）里面执行。
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case GETINFOS_SUCCESS: {
                    if (isGetFIleInfo){
                        break;
                    }
                    isGetFIleInfo=true;
                    String clientFiles_str = (String) msg.obj;
                    Gson gson = new Gson();
                    getFilesInfos = gson.fromJson(clientFiles_str, new TypeToken<List<ServiceFileInfo>>() {
                    }.getType());
                    String phoneId=getFilesInfos.get(0).getPhoneId();
                    HashMap<String,ArrayList<ServiceFileInfo>> hashMap= DowningFileInfoFromSdUtil.get();
                    ArrayList<ServiceFileInfo> serviceFileInfos=hashMap.get(phoneId);
                    if(serviceFileInfos!=null&&serviceFileInfos.size()>0){
                        int tag=0;
                        for (int i=0;i<serviceFileInfos.size();i++){
                            tag=0;
                            for (int j=0;j<getFilesInfos.size();j++){
                                if(serviceFileInfos.get(i).getUuid().equals(getFilesInfos.get(j).getUuid())){
                                    tag=1;
                                }
                            }
                            if (tag==0){
                                serviceFileInfos.remove(i);
                            }
                        }
                        if(serviceFileInfos.size()==0){
                            hashMap.remove(phoneId);
                            DowningFileInfoFromSdUtil.save(hashMap);
                        }else{
                            Intent intent=new Intent(ShowShareFileInfoActivity.this,DownFilesActivity.class);
                            intent.putExtra("downFiles", serviceFileInfos);
                            intent.putExtra("downhalfFile",1);
                            startActivity(intent);
                        }
                    }
                    saveItemClick=new boolean[getFilesInfos.size()];
                    for (int i=0;i<saveItemClick.length;i++){
                        saveItemClick[i]=false;
                    }
                    lv.setAdapter(new MyAdapter());
                    lv.setOnItemClickListener(ShowShareFileInfoActivity.this);
                    break;
                }
            }
        }

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_share_info);
        lv = (ListView) findViewById(R.id.lv_show_file_info_lv);
        btn_downfiles=(TextView)findViewById(R.id.btn_downfiles);
        ImageView iv_back=(ImageView)findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        btn_downfiles.setOnClickListener(this);
        initGetFileInfo();
        getFileInfoHandler.post(runnable);
        getFileInfoHandler.postDelayed(threadRemove,15000);
    }
    private void initGetFileInfo() {
/*        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        int ip = dhcpInfo.gateway;
        byte[] byteAdd = new byte[4];
        byteAdd[0] = (byte) (0xff & ip);
        byteAdd[1] = (byte) ((0xff00 & ip) >> 8);
        byteAdd[2] = (byte) ((0xff0000 & ip) >> 16);
        byteAdd[3] = (byte) ((0xff000000 & ip) >> 24);
        InetAddress address=null;
        try
        {
            address = Inet4Address.getByAddress(byteAdd);
        } catch (UnknownHostException e)
        {
            e.printStackTrace();
        }*/
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
        String ip = Ipint2StringUtil.int2StringIp(dhcpInfo.gateway);
       /* ClientGetFileInfosHandler clientGetFileInfosHandler=new ClientGetFileInfosHandler(address,uiHandler);
        clientGetFileInfosHandler.setPriority(Thread.MAX_PRIORITY);
        clientGetFileInfosHandler.start();*/
        NioGetFileInfosClient nioGetFileInfosClient=new NioGetFileInfosClient(ip,uiHandler);
        try {
            nioGetFileInfosClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_downfiles:{
                if (downFiles.size()<=0){
                    BaseApplication.showToast("请先选择要下载的文件");
                    return;
                }
                Intent intent=new Intent(ShowShareFileInfoActivity.this,NioDownFilesActivity.class);
                intent.putExtra("downFiles", downFiles);
                startActivity(intent);
                break;
            }
            case R.id.iv_back:
                this.finish();
                break;
        }
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder viewHolder=(ViewHolder)view.getTag();
        if (viewHolder.check.getVisibility()==View.GONE){
            downFiles.add(getFilesInfos.get(position));
            saveItemClick[position]=true;
            viewHolder.check.setVisibility(View.VISIBLE);
        }else{
            for (int j=0;j<downFiles.size();j++){
                if(getFilesInfos.get(position).getUuid().equals(downFiles.get(j).getUuid())){
                    downFiles.remove(j);
                }
            }
            saveItemClick[position]=false;
            viewHolder.check.setVisibility(View.GONE);
        }
    }
    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return getFilesInfos.size();
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            ServiceFileInfo item = getFilesInfos.get(position);
            if (convertView == null) {
                convertView = View.inflate(ShowShareFileInfoActivity.this, R.layout.file_info_item, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_Img = (ImageView) convertView.findViewById(iv_sendImg);
                viewHolder.tv_size = (TextView) convertView.findViewById(tv_sendSize);
                viewHolder.tv_fileName = (TextView) convertView.findViewById(tv_sendFileName);
                viewHolder.check=(ImageView)convertView.findViewById(R.id.showfileinfo_check);
                convertView.setTag(viewHolder);
            } else {
                viewHolder =  (ViewHolder)convertView.getTag();
            }
            viewHolder.check.setVisibility(saveItemClick[position]?View.VISIBLE:View.GONE);
            viewHolder.iv_Img.setImageDrawable(IcoUtil.getSimpleIco(item.getFileType(),item.getFilepath()));
            viewHolder.tv_fileName.setText(item.getFileName());
            viewHolder.tv_size.setText(FileSizeFormatUtil.format(item.getTransRange().getEndByte() - item.getTransRange().getBeginByte()));
            return convertView;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
    }
    class ViewHolder{
        public  ImageView iv_Img;
        public TextView tv_size;
        public TextView tv_fileName;
        public ImageView check;
    }
   /* @Override
    protected void onDestroy() {
        WifiHelper wifiHelper=new WifiHelper();
        if (wifiHelper.isWifiEnabled())
        wifiHelper.setWifiEnabled(false);
        super.onDestroy();
    }*/
}

