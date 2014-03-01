package com.dsht.kerneltweaker;

import com.dsht.settings.SettingsFragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class GlossaryArrayAdapter extends BaseAdapter {

	Context mContext;
    int layoutResourceId;
    String[] titles;
    String[] descs;
    String color;
    
    public GlossaryArrayAdapter(Context mContext, int layoutResourceId, String[] titles, String[] descs, String color) {

        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.titles = titles;
        this.descs = descs;
        this.color = color;
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
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(v == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            v = inflater.inflate(layoutResourceId, parent, false);
		}
		TextView text1 = (TextView) v.findViewById(android.R.id.text1);
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			text1.setTextColor(color);
		} else {
			int col = Color.parseColor(color);
			text1.setTextColor(col);
		}
		text1.setTextAppearance(mContext, android.R.attr.textAppearanceListItemSmall);
        TextView text2 = (TextView) v.findViewById(android.R.id.text2);
        text1.setText(titles[position]);
        text2.setText(descs[position]);
		return v;
	}

}

