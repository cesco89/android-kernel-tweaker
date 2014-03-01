package com.dsht.kerneltweaker;

import java.io.File;
import java.util.Date;

import com.dsht.settings.SettingsFragment;

import android.content.Context;
import android.graphics.Color;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class FileBaseAdapter extends BaseAdapter {

	private Context mContext;
	private File[] files;
	
	public FileBaseAdapter(Context context, File[] filesList) {
		this.mContext = context;
		this.files = filesList;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return files.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return files[position];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		if (v == null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.file_list_item, parent, false);
		}
		ImageView icon = (ImageView) v.findViewById(R.id.icon);
		TextView title = (TextView) v.findViewById(R.id.name);
		TextView info = (TextView) v.findViewById(R.id.info);
		
		if(files[position].isDirectory()) {
			icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_folder));
			long lastModified = files[position].lastModified();
			Date date = new Date(lastModified);
			info.setText(date+"");
		} else {
			icon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file));
			long lastModified = files[position].lastModified();
			Date date = new Date(lastModified);
			long size = files[position].length();
			info.setText(date+"   "+Formatter.formatFileSize(mContext, size));
		}
		title.setText(files[position].getName());
		
		
		return v;
	}

}
