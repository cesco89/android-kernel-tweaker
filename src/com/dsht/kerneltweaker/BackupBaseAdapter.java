package com.dsht.kerneltweaker;

import java.io.File;
import java.util.List;

import com.dsht.settings.SettingsFragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BackupBaseAdapter extends BaseAdapter {

	Context mContext;
	private List<File> files;

	public BackupBaseAdapter(Context context, List<File> listFiles) {
		this.mContext = context;
		this.files = listFiles;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return files.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void update(List<File> files) {
		this.files.clear();
		this.files = files;
		this.notifyDataSetChanged();
	}

	public void remove(Object object){
		files.remove(object);
	}

	public void add(Object object){
		files.add((File)object);
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(v==null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.backup_list_item, parent, false);
		}


		TextView title = (TextView) v.findViewById(R.id.filename);
		title.setText(files.get(position).getName());
		ImageView drag = (ImageView) v.findViewById(R.id.image);

		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			title.setTextColor(color);
			drag.setColorFilter(color);
		}else if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_BAK, 0);
			title.setTextColor(color);
			drag.setColorFilter(color);
		}
		return v;
	}

}
