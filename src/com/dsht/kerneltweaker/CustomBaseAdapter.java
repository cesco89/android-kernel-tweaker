package com.dsht.kerneltweaker;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CustomBaseAdapter extends BaseAdapter {

	Context mContext;
	String[] values;
	String[] names;
	String[] summaries;
	String color;
	String FILE;
	DatabaseHandler db;
	int newValue;
	SharedPreferences mPrefs;

	public CustomBaseAdapter(Context mContext, String[] items, String[] names,
			String[] summaries, String file, String color, DatabaseHandler db) {
		this.mContext = mContext;
		this.values = items;
		this.names = names;
		this.summaries = summaries;
		this.FILE = file;
		this.color = color;
		this.db = db;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return values.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return values[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public String[] getValues() {
		return this.values;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View v = convertView;
		if(v==null) {
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.preference_widget_seekbar, parent, false);
		}
		
		TextView title = (TextView) v.findViewById(R.id.title);
		TextView summary = (TextView) v.findViewById(R.id.summary);
		final TextView currValue = (TextView) v.findViewById(R.id.currvalue);
		SeekBar seekbar = (SeekBar) v.findViewById(R.id.seekbar);
		title.setText(names[position]);
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
		summary.setText(summaries[position]);
		seekbar.setMax(200);
		int sbValue = Integer.parseInt(values[position])*4/1024;
		Log.d("VALUE "+ position, sbValue+"");
		seekbar.setProgress(sbValue);
		currValue.setText(sbValue+" Mb");
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar sb, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				newValue = sb.getProgress();
						//((sb.getProgress()*1024)/4);
				//values[position] = new String(String.valueOf(newValue));
				currValue.setText(sb.getProgress()+" Mb");
				
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar sb) {
				// TODO Auto-generated method stub
				int finalValue = ((sb.getProgress()*1024)/4);
				values[position] = new String(String.valueOf(finalValue));
				Log.d("STRING", buildString(values));
				CommandCapture command = new CommandCapture(0,"echo \""+buildString(values)+"\" > "+FILE);
				try {
					RootTools.getShell(true).add(command);
					List<DataItem> items = db.getAllItems();
					for(DataItem item : items) {
						if(item.getName().toString().contains(FILE)) {
							db.deleteItemByName("'"+FILE+"'");
							db.addItem(new DataItem("'"+FILE+"'", 
									buildString(values), 
									"LOW MEMORY KILLER",
									"lmk"));
							break;
						}
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (RootDeniedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		});
		return v;
	}
	
	public String buildString(String[] values) {
		String builded = "";
		for(int i = 0; i<values.length; i++) {
			if(i!=values.length-1) {
				builded+=values[i]+",";
			}else {
				builded+=values[i];
			}
		}
		return builded;
	}

}
