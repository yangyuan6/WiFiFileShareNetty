package com.yangyuan.wififileshareNio.choser;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.yangyuan.wififileshareNio.Base.BaseFragment;
import com.yangyuan.wififileshareNio.R;
import com.yangyuan.wififileshareNio.Utils.TranTool;
import com.yangyuan.wififileshareNio.bean.FileType;
import com.yangyuan.wififileshareNio.bean.Range;
import com.yangyuan.wififileshareNio.bean.SendStatus;
import com.yangyuan.wififileshareNio.bean.ServiceFileInfo;
import com.yangyuan.wififileshareNio.sendReciver.FileChoseChangedReciver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by CFun on 2015/4/21.
 */
//图片选择
public class ImageFileChoser extends BaseFragment implements AdapterView.OnItemClickListener, GetChoseFile
{
	private GridView gridView;
	private Context context;
	private View mView;
	private HashSet<String> selectPath = new HashSet<>();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (mView != null)
		{
			ViewGroup parent = (ViewGroup) mView.getParent();
			if (parent != null)
			{
				parent.removeView(mView);
			}
			return mView;
		}
		context = inflater.getContext();
		gridView = new GridView(context);
		gridView.setNumColumns(2);
		gridView.setAdapter(new ImageAdapter(context, getImageCursor(context), false));
		gridView.setOnItemClickListener(this);
		gridView.setVerticalSpacing(10);
		gridView.setHorizontalSpacing(10);
		mView = gridView;
		return gridView;
	}


	private Cursor getImageCursor(Context context)
	{
		// 指定要查询的uri资源
		Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		// 获取ContentResolver
		ContentResolver contentResolver = context.getContentResolver();
		// 查询的字段
		String[] projection = {MediaStore.Images.Media._ID,
				MediaStore.Images.Media.DATA};
		// 条件
//		String selection = MediaStore.Images.Media.MIME_TYPE + "=?";
		// 条件值(這裡的参数不是图片的格式，而是标准，所有不要改动)
//		String[] selectionArgs = {"image/jpeg"};
		// 排序
		String sortOrder = MediaStore.Images.Media.DATE_MODIFIED + " desc";
		// 查询sd卡上的图片
		Cursor cursor = contentResolver.query(uri, projection, null, null, sortOrder);
		return cursor;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		ViewHolder holder = (ViewHolder)view.getTag();
		if(holder.check.getVisibility() == View.VISIBLE)
		{
			holder.check.setVisibility(View.GONE);
			//添加文件信息键值对
			selectPath.remove(holder.path);
			//发送文件信息改变的广播
			FileChoseChangedReciver.sendBroadcast(false);
		}
		else
		{
			holder.check.setVisibility(View.VISIBLE);
			selectPath.add(holder.path);
			FileChoseChangedReciver.sendBroadcast(true);
		}
	}
	//GetChoseFile接口实现
	@Override
	public Collection<ServiceFileInfo> getChosedFile()
	{
		LinkedList<ServiceFileInfo> sendFileInfos = new LinkedList<>();
		Iterator<String> iterator = selectPath.iterator();
		while (iterator.hasNext())
		{
			String path = iterator.next();
			ServiceFileInfo info = new ServiceFileInfo();
			info.setFileType(FileType.image);
			info.setFilepath(path);
			info.setTransRange(Range.getByPath(path));
			info.setSendStatu(SendStatus.SenddingBegin);
			info.setFileDesc(TranTool.getFileNameByPath(path));
			info.setSendPercent(0);
			sendFileInfos.add(info);
		}
		return sendFileInfos;
	}

	class ImageAdapter extends CursorAdapter
	{
		LayoutInflater inflater;

		public ImageAdapter(Context context, Cursor c, boolean autoRequery)
		{
			super(context, c, autoRequery);
			inflater = LayoutInflater.from(context);
		}

		public ImageAdapter(Context context, Cursor c, int flags)
		{
			super(context, c, flags);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent)
		{
			ViewHolder holder = new ViewHolder();

//			holder.id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID));
			holder.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			View view = inflater.inflate(R.layout.image_choser_item, null);

			holder.img = (ImageView) view.findViewById(R.id.img);
			holder.check = (ImageView) view.findViewById(R.id.check);

			view.setTag(holder);

//			holder.img.setImageDrawable(IcoUtil.getImgDrawable(holder.data.path, MediaStore.Images.Thumbnails.MINI_KIND, holder.data.id));
			System.out.println(holder.path+holder.img);
			ImageLoader.getInstance().displayImage("file://"+holder.path, holder.img);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor)
		{
			ViewHolder holder = (ViewHolder) view.getTag();


			holder.path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));

			holder.check.setVisibility(selectPath.contains(holder.path) ? View.VISIBLE : View.GONE);
			ImageLoader.getInstance().displayImage("file://"+holder.path, holder.img);
		}


	}

	class ViewHolder
	{
		public String path;
		public ImageView img;
		public ImageView check;
	}

}
