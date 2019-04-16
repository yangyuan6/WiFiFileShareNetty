package com.yangyuan.wififileshareNio.UI;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.yangyuan.wififileshareNio.BaseApplication;
import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.Utils.FileSizeFormatUtil;
import com.yangyuan.wififileshareNio.Utils.GetServiceFileInfosFromSdUtil;
import com.yangyuan.wififileshareNio.Utils.IcoUtil;
import com.yangyuan.wififileshareNio.Utils.SaveFileInfo2SdUtil;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import java.util.ArrayList;
import static com.yangyuan.wififileshareNio.R.id.iv_sendImg;
import static com.yangyuan.wififileshareNio.R.id.tv_sendFileName;
import static com.yangyuan.wififileshareNio.R.id.tv_sendSize;

/**
 * Created by yangy on 2017/3/5.
 */

public class ManageShareFileActivity extends Activity implements View.OnClickListener,AdapterView.OnItemClickListener {
    private ArrayList<ServiceFileInfo> serviceFileInfos;
    private ListView listView;
    private ArrayList<ServiceFileInfo> removeFileInfos=new ArrayList<>();
    private TextView btn_removesharefiles;
    private boolean [] saveItemClick;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_sharefile);
        listView=(ListView)findViewById(R.id.lv_share_file_info);
        btn_removesharefiles=(TextView)findViewById(R.id.btn_removesharefiles);
        btn_removesharefiles.setOnClickListener(this);
        serviceFileInfos= GetServiceFileInfosFromSdUtil.doAction();
        saveItemClick=new boolean[serviceFileInfos.size()];
        TextView tv_title=(TextView)findViewById(R.id.tv_title) ;
        tv_title.setText("分享列表");
        ImageView iv_back=(ImageView)findViewById(R.id.iv_back);
        iv_back.setOnClickListener(this);
        listView.setAdapter(new MyAdapter());
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_removesharefiles:{
                if (removeFileInfos.size()<=0){
                    BaseApplication.showToast("请先选择要取消分享的文件");
                    return;
                }else{
                    for (ServiceFileInfo serviceFileInfo:removeFileInfos){
                        for (int i=0;i<serviceFileInfos.size();i++){
                            if (serviceFileInfo.getUuid().equals(serviceFileInfos.get(i).getUuid())){
                                serviceFileInfos.remove(i);
                            }
                        }
                    }
                    SaveFileInfo2SdUtil.save(serviceFileInfos);
                    removeFileInfos.clear();
                    finish();
                }
            }
            case R.id.iv_back: {
                this.finish();
                break;
            }
        }
    } @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ViewHolder viewHolder=(ViewHolder)view.getTag();
        if (viewHolder.check.getVisibility()==View.GONE){
            removeFileInfos.add(serviceFileInfos.get(position));
            viewHolder.check.setVisibility(View.VISIBLE);
            saveItemClick[position]=true;
        }else{
            for (int j=0;j<removeFileInfos.size();j++){
                if(serviceFileInfos.get(position).getUuid().equals(removeFileInfos.get(j).getUuid())){
                    removeFileInfos.remove(j);
                }
            }
            viewHolder.check.setVisibility(View.GONE);
            saveItemClick[position]=false;
        }

    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return serviceFileInfos.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            ServiceFileInfo item = serviceFileInfos.get(position);
            if (convertView == null) {
                convertView = View.inflate(ManageShareFileActivity.this, R.layout.file_info_item, null);
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
}
