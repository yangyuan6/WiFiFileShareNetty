package com.yangyuan.wififileshareNio.UI;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
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
import com.yangyuan.wififileshareNio.Utils.GetFileReceiveHistoriesUtil;
import com.yangyuan.wififileshareNio.Utils.IcoUtil;
import com.yangyuan.wififileshareNio.Utils.OpenFiles;
import com.yangyuan.wififileshareNio.Utils.SaveFileReceiveHistories2SdUtil;
import com.yangyuan.wififileshareNio.bean.FileReceiveHistory;
import com.yangyuan.wififileshareNio.bean.FileType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
/**
 * Created by yangy on 2017/3/8.
 */

public class ReceiveHistoryActivity extends Activity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private ImageView back;
    private TextView title;
    private ListView listView;
    private TextView cleanAll;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recive_history);
        back = (ImageView) findViewById(R.id.iv_back);
        title = (TextView) findViewById(R.id.tv_title);
        listView = (ListView) findViewById(R.id.listView);
        cleanAll = (TextView) findViewById(R.id.cleanAll);
        cleanAll.setOnClickListener(this);
        back.setOnClickListener(this);
        title.setText("传输历史");
        List<FileReceiveHistory> entries=new ArrayList<>();
        try {
            entries= GetFileReceiveHistoriesUtil.doAction();
        } catch (Exception e) {
            BaseApplication.showToast("未下载过文件");
            e.printStackTrace();
            finish();
        }
        HistoryAdapter adapter = new HistoryAdapter(entries);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (holder.data.getExists())
            OpenFiles.openFile(this, new File(holder.data.getPath()));
        else
            BaseApplication.showToast("文件已被移动或删除，无法打开");
    }
    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.cleanAll:
                SaveFileReceiveHistories2SdUtil.doAction(new ArrayList<FileReceiveHistory>());
                BaseApplication.showToast("传输历史已清空");

                finish();
                break;
            case R.id.iv_back:
                finish();
                break;
        }
    }
    class HistoryAdapter extends BaseAdapter
    {
        private List<FileReceiveHistory> entries = null;
        private LayoutInflater inflater;

        public HistoryAdapter(List<FileReceiveHistory> entries)
        {
            this.entries = entries;
            inflater = LayoutInflater.from(ReceiveHistoryActivity.this);
        }
        @Override
        public int getCount()
        {
            return entries == null ? 0 : entries.size();
        }
        @Override
        public Object getItem(int position)
        {
            return entries.get(position);
        }
        @Override
        public long getItemId(int position)
        {
            return position;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            if (convertView == null)
            {
                ViewHolder holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.recive_history_list_item, null);
                holder.ico = (ImageView) convertView.findViewById(R.id.ico);
                holder.desc = (TextView) convertView.findViewById(R.id.desc);
                holder.path = (TextView) convertView.findViewById(R.id.path);

                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();

            holder.data = entries.get(position);//数据绑定
            holder.size = (TextView) convertView.findViewById(R.id.size);

            //利用数据设置控件
            holder.ico.setImageDrawable(IcoUtil.getIco(FileType.valueOf(holder.data.getFileType()), holder.data.getPath()));
            String pathShow=holder.data.getPath().replace(Environment.getExternalStorageDirectory().getPath()+"/","");
            holder.path.setText(pathShow);
            holder.desc.setText(holder.data.getFileName());
            holder.size.setText(FileSizeFormatUtil.format(holder.data.getSize()));
            holder.data.setExists(new File(holder.data.getPath()).exists());
            if (holder.data.getExists())
            {
                holder.desc.setTextColor(holder.size.getCurrentTextColor());
                holder.desc.getPaint().setFlags(Paint.DEV_KERN_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            } else
            {
                holder.desc.setTextColor(Color.RED);
                holder.desc.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
            }
            return convertView;
        }
    }

    class ViewHolder
    {
        public FileReceiveHistory data;
        public ImageView ico;
        public TextView desc;
        public TextView path;
        public TextView size;
    }
}