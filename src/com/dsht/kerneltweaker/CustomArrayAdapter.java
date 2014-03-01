package com.dsht.kerneltweaker;

import com.dsht.settings.SettingsFragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomArrayAdapter extends BaseAdapter {

	Context mContext;
	int layoutResourceId;
	String[] titles;
	String[] descs;
	String[] colors;
	int[] icons;
	SharedPreferences mPrefs;

	public CustomArrayAdapter(Context mContext, int layoutResourceId, String[] titles, String[] descs, String[] colors, int[] icons) {

		this.layoutResourceId = layoutResourceId;
		this.mContext = mContext;
		this.titles = titles;
		this.descs = descs;
		this.colors = colors;
		this.icons = icons;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return titles.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2; //return 2, you have two types that the getView() method will return, normal(0) and for the last row(1)
	}

	@Override
	public int getItemViewType(int position) {
		return (titles[position].contains("--")) ? 1 : 0; //if we are at the last position then return 1, for any other position return 0
	}

	@Override
	public boolean isEnabled(int position) {
		if(getItemViewType(position) == 1) {
			return false;
		}
		return true;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		int type = getItemViewType(position);
		if(v == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			if(type == 0) {
				v = inflater.inflate(layoutResourceId, parent, false);
			}else if(type == 1) {
				v = inflater.inflate(R.layout.menu_header, parent, false);
			}
		}
		if(type == 0) {
			TextView text1 = (TextView) v.findViewById(android.R.id.text1);
			ImageView image = (ImageView) v.findViewById(R.id.image);
			image.setImageDrawable(mContext.getResources().getDrawable(icons[position]));
			image.setColorFilter(Color.parseColor("#FFFFFF"));
			if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
				int color = mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
				text1.setTextColor(color);
				image.setColorFilter(color);
			}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
				int color = Color.parseColor("#ff0099cc");
				switch(position) {
				case 1:
					color = mPrefs.getInt(SettingsFragment.KEY_STAT, Color.parseColor("#FFFFFF"));
					break;
				case 2:
					color = mPrefs.getInt(SettingsFragment.KEY_INFO, Color.parseColor("#FFFFFF"));
					break;
				case 4:
					color = mPrefs.getInt(SettingsFragment.KEY_CPU, Color.parseColor("#FFFFFF"));
					break;
				case 5:
					color = mPrefs.getInt(SettingsFragment.KEY_GPU, Color.parseColor("#FFFFFF"));
					break;
				case 6:
					color = mPrefs.getInt(SettingsFragment.KEY_UV, Color.parseColor("#FFFFFF"));
					break;
				case 8:
					color = mPrefs.getInt(SettingsFragment.KEY_KERNEL, Color.parseColor("#FFFFFF"));
					break;
				case 9:
					color = mPrefs.getInt(SettingsFragment.KEY_LMK, Color.parseColor("#FFFFFF"));
					break;
				case 10:
					color = mPrefs.getInt(SettingsFragment.KEY_VM, Color.parseColor("#FFFFFF"));
					break;
				case 12:
					color = mPrefs.getInt(SettingsFragment.KEY_REVIEW, Color.parseColor("#FFFFFF"));
					break;
				case 14:
					color = mPrefs.getInt(SettingsFragment.KEY_FILE, Color.parseColor("#FFFFFF"));
					break;
				case 15:
					color = mPrefs.getInt(SettingsFragment.KEY_BAK, Color.parseColor("#FFFFFF"));
					break;
				case 16:
					color = mPrefs.getInt(SettingsFragment.KEY_RECOVERY, Color.parseColor("#FFFFFF"));
					break;
				case 18:
					color = mPrefs.getInt(SettingsFragment.KEY_PROP, Color.parseColor("#FFFFFF"));
					break;
				case 19:
					color = mPrefs.getInt(SettingsFragment.KEY_INIT, Color.parseColor("#FFFFFF"));
					break;
				case 20:
					color = mPrefs.getInt(SettingsFragment.KEY_BLUR, Color.parseColor("#FFFFFF"));
					break;
				}
				text1.setTextColor(color);
				image.setColorFilter(color);
			}else {
				int color = Color.parseColor("#ff0099cc");
				text1.setTextColor(color);
				image.setColorFilter(color);
			}
			text1.setText(titles[position]);
		}
		else if(type == 1) {
			TextView header = (TextView) v.findViewById(R.id.menu_header);
			header.setText(titles[position].replaceAll("--", ""));
			header.setClickable(false);
			boolean light = mPrefs.getBoolean(SettingsFragment.KEY_THEME, false);
			if(light){
				header.setTextColor(Color.BLACK);
			}else {
				header.setTextColor(Color.WHITE);
			}
		}
		return v;
	}

}

