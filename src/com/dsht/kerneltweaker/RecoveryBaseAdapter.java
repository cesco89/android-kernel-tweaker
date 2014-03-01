package com.dsht.kerneltweaker;

import java.util.ArrayList;

import com.dsht.settings.SettingsFragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class RecoveryBaseAdapter extends BaseAdapter {

	Context mContext;
	ArrayList<String> mNames;
	ArrayList<String> mValues;

	public RecoveryBaseAdapter(Context context, ArrayList<String> names, ArrayList<String> values) {
		this.mContext = context;
		this.mNames = names;
		this.mValues = values;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mNames.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mNames.get(position);
	}
	
	public String getNameItem(int position) {
		// TODO Auto-generated method stub
		return mNames.get(position);
	}
	
	public String getValueItem(int position) {
		// TODO Auto-generated method stub
		return mValues.get(position);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void insert(String name, String value, int nameindex, int valueindex) {
		if (mNames != null && mValues != null) {
			mNames.add(nameindex, name);
			mValues.add(valueindex, value);
			notifyDataSetChanged();

		} else {
			mNames.add(nameindex, name);
			mValues.add(valueindex, value);
			notifyDataSetChanged();
		}
	}

	public void remove(String removename, String removevalue) {
		if (mNames != null && mValues != null) {
			mNames.remove(removename);
			mValues.remove(removevalue);
			notifyDataSetChanged();
		} else {
			mNames.remove(removename);
			mValues.remove(removevalue);
			notifyDataSetChanged();
		}
	}


	@Override
	public View getView(int position, View v, ViewGroup parent) {
		// TODO Auto-generated method stub
		if(v== null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.dragsort_list_item, parent, false);
		}
		TextView title = (TextView) v.findViewById(R.id.name);
		ImageView image = (ImageView) v.findViewById(R.id.image);
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			title.setTextColor(color);
			image.setColorFilter(color);
		}else if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_RECOVERY, Color.parseColor("#ff0099cc"));
			title.setTextColor(col);
			image.setColorFilter(col);
		} 
		else {
			int color = Color.parseColor( mContext.getResources().getStringArray(R.array.menu_colors)[position]);
			title.setTextColor(color);
			image.setColorFilter(color);
		}
		TextView command = (TextView) v.findViewById(R.id.value);
		title.setText(mNames.get(position));
		command.setText(mValues.get(position));
		return v;
	}

}
