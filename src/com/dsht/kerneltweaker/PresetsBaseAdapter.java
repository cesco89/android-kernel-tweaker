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
import android.widget.TextView;

public class PresetsBaseAdapter extends BaseAdapter {
	
	String[] presetValues;
	String[] presetNames;
	Context mContext;
	SharedPreferences mPrefs;

	public PresetsBaseAdapter(Context con, String[] values, String[] names) {
		this.presetValues = values;
		this.presetNames = names;
		this.mContext = con;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return presetValues.length;
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return presetValues[arg0];
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(v == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			v = inflater.inflate(R.layout.list_item, parent, false);
		}
		TextView title = (TextView) v.findViewById(android.R.id.text1);
		TextView summary = (TextView) v.findViewById(android.R.id.text2);
		title.setText(presetNames[position]);
		summary.setText(presetValues[position]);
		if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int color = mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			title.setTextColor(color);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_LMK, Color.parseColor("#ff0099cc"));
			title.setTextColor(col);
		} 
		else {
			int color = Color.parseColor( mContext.getResources().getStringArray(R.array.menu_colors)[6]);
			title.setTextColor(color);
		}
		return v;
	}

}
