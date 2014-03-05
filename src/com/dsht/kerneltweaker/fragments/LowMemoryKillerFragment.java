package com.dsht.kerneltweaker.fragments;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dsht.kerneltweaker.CustomBaseAdapter;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.PresetsBaseAdapter;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class LowMemoryKillerFragment extends Fragment {

	private static final String MINFREE_FILE = "/sys/module/lowmemorykiller/parameters/minfree";
	private String[] values;
	private CustomBaseAdapter mAdapter;
	private Context mContext;
	private MenuItem boot;
	private DatabaseHandler db = MainActivity.db;
	private List<DataItem> items;
	private String category = "lmk";
	private String color;
	private ListView list;
	private String[] names;
	private String[] summaries;
    
    private final static String[] presets = {
    	"512,1024,1280,2048,3072,4096",
    	"1024,2048,2560,4096,6144,8192",
    	"1024,2048,4096,8192,12288,16384",
    	"2048,4096,8192,16384,24576,32768",
    	"2048,4096,8192,16384,24576,32768"
    	};
    private static final String[] presetsNames = {
    	"Very Light",
    	"Light",
    	"Medium",
    	"Aggressive",
    	"Very Aggressive"
    };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		setHasOptionsMenu(true);
		items = db.getAllItems();
		color = getResources().getStringArray(R.array.menu_colors)[4];
		Helpers.setPermissions(MINFREE_FILE);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.lmk_layout, container,false);
		list = (ListView) v.findViewById(R.id.list);
		values = getMinFreeValues();
		names = mContext.getResources().getStringArray(R.array.lmk_titles);
		summaries = mContext.getResources().getStringArray(R.array.lmk_descs);
		mAdapter = new CustomBaseAdapter(mContext, 
				values, 
				names, 
				summaries,
				MINFREE_FILE, 
				color,
				db);
		
		list.setAdapter(mAdapter);
		View footer = inflater.inflate(R.layout.lmk_footer, null, false);
		LinearLayout ll = (LinearLayout) footer.findViewById(R.id.preset_layout);
		addPresets(ll, inflater);
		
		list.addFooterView(footer);
		MainActivity.menu.toggle(true);
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_lmk, menu);
		boot = (MenuItem) menu.findItem(R.id.action_boot);
		for (DataItem item : items) {
			if(item.getName().contains(MINFREE_FILE)) {
				boot.setChecked(true);
				break;
			}
			boot.setChecked(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {

		case R.id.action_boot:
			if(item.isChecked()) {
				db.deleteItemByName("'"+MINFREE_FILE+"'");
				item.setChecked(false);
			}else {
				db.deleteItemByName("'"+MINFREE_FILE+"'");
				db.addItem(new DataItem("'"+MINFREE_FILE+"'", 
						buildString(mAdapter.getValues()), 
						"LOW MEMORY KILLER",
						category));

				item.setChecked(true);
			}
			return true;
		}
		return false;
	}

	private String[] getMinFreeValues() {
		String content = Helpers.readFileViaShell(MINFREE_FILE, false);
		Log.d("CONTENT", content);
		String[] vals = content.trim().replace("\n", "").split(",");
		return vals;
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
	
	private void addPresets(LinearLayout ll, LayoutInflater inflater) {
		for(int i = 0; i < presetsNames.length; i++) {
			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			View child = inflater.inflate(R.layout.list_item, null, false);
			TextView title = (TextView) child.findViewById(android.R.id.text1);
			final TextView summary = (TextView) child.findViewById(android.R.id.text2);
			title.setText(presetsNames[i]);
			summary.setText(presets[i]);
			
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
			
			child.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					CMDProcessor.runSuCommand("echo \""+ summary.getText().toString() + "\" > "+ MINFREE_FILE );
					values = getMinFreeValues();
					mAdapter = new CustomBaseAdapter(mContext, 
							values, 
							names, 
							summaries,
							MINFREE_FILE, 
							color,
							db);
					list.setAdapter(mAdapter);
				}
				
			});
			ll.addView(child);
		}
	}

}
