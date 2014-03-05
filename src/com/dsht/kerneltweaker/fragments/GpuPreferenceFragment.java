package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.CustomListPreference;
import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;

public class GpuPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener {
	private Context mContext;
	private SharedPreferences mPrefs;
	private CustomListPreference mGpuFrequency;
	private PreferenceCategory mCategory;
	private static final String GPU_FREQUENCIES_FILE = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
	private static final String GPU_FOLDER = "/sys/class/kgsl/";
	private static final String GPU_MAX_FREQ_FILE = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
	private String color;
	private PreferenceScreen mRoot;
	private DatabaseHandler db = MainActivity.db;
	private static final String category = "gpu";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_gpu);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle();
		}
		
		Helpers.setPermissions(GPU_FREQUENCIES_FILE);
		Helpers.setPermissions(GPU_MAX_FREQ_FILE);
		Helpers.setPermissions(GPU_FOLDER);
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mCategory = (PreferenceCategory) findPreference("key_gpu_category");
		mGpuFrequency = (CustomListPreference) findPreference("key_gpu_frequency");
		mRoot = (PreferenceScreen) findPreference("key_root");
		mGpuFrequency.setOnPreferenceChangeListener(this);
		//mGpuFrequency.setSummary(mPrefs.getString(mGpuFrequency.getKey(), "Set GPU maximum frequency"));
		color = "";
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			color = "#"+Integer.toHexString(col);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GPU, Color.parseColor("#ff0099cc"));
			color = "#"+Integer.toHexString(col);
		} 
		else {
			color = getResources().getStringArray(R.array.menu_colors)[3];
		}
		mGpuFrequency.setTitleColor(color);
		mGpuFrequency.setCategory(category);
		mGpuFrequency.setKey(GPU_MAX_FREQ_FILE);
		
		if(new File(GPU_FOLDER).exists()) {
			addPreferences();
			String gpuFrequencies = Helpers.getFileContent(new File(GPU_FREQUENCIES_FILE));
			String[] frequencies = gpuFrequencies.split(" ");
			String[] gpuNames = Helpers.getFreqToMhz(GPU_FREQUENCIES_FILE);
			mGpuFrequency.setEntries(gpuNames);
			mGpuFrequency.setEntryValues(frequencies);
			mGpuFrequency.setSummary(Helpers.getFileContent(new File(GPU_MAX_FREQ_FILE)));
			mGpuFrequency.setValue(Helpers.getFileContent(new File(GPU_MAX_FREQ_FILE)));
		} else {
			mCategory.removePreference(mGpuFrequency);
			CustomPreference pref = new CustomPreference(mContext, true, category);
			pref.setTitle("No Tweakable Values");
			pref.setSummary("This kernel doesn\'t support GPU Tweaks");
			pref.setTitleColor("#ff4444");
			pref.setSummaryColor("#ff4444");
			pref.excludeFromDialog(true);
			mCategory.addPreference(pref);
		}
		
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);
		v.findViewById(android.R.id.list);

		return v;
	}


	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		if(pref == mGpuFrequency) {
			String value = ((String) newValue).trim();
			mGpuFrequency.setSummary(value);
			mGpuFrequency.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+GPU_MAX_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
			Log.d("TAG",""+((CustomListPreference) pref).isBootChecked());
		}
		return false;
	}


	private void addPreferences() {
		File f = new File("/sys/module/msm_kgsl_core/parameters/down_threshold");
		File f2 = new File("/sys/module/msm_kgsl_core/parameters/sample_time_ms");
		File f3 = new File("/sys/module/msm_kgsl_core/parameters/up_threshold");
		File f4 = new File("/sys/module/msm_kgsl_core/parameters/simple_laziness");
		File f5 = new File("/sys/module/msm_kgsl_core/parameters/simple_ramp_threshold");
		if(f.exists())
			createPreference(f, color);
		if(f2.exists())
			createPreference(f2, color);
		if(f3.exists())
			createPreference(f3, color);
		if(f4.exists())
			createPreference(f4, color);
		if(f5.exists())
			createPreference(f5, color);

	}

	private void createPreference(File file, String color) {
		String fileName = file.getName();
		String filePath = file.getAbsolutePath();
		final String fileContent = Helpers.getFileContent(file);
		final CustomPreference pref = new CustomPreference(mContext, false, category);
		pref.setTitle(fileName);
		pref.setTitleColor(color);
		pref.setSummary(fileContent);
		pref.setKey(filePath);
		Log.d("CONTENT", fileContent);
		mCategory.addPreference(pref);
		pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

			@Override
			public boolean onPreferenceClick(final Preference p) {
				// TODO Auto-generated method stub
				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				LayoutInflater inflater = getActivity().getLayoutInflater();
				View v = inflater.inflate(R.layout.dialog_layout, null, false);
				final EditText et = (EditText) v.findViewById(R.id.et);
				String val = p.getSummary().toString();
				et.setText(val);
				et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
				et.setGravity(Gravity.CENTER_HORIZONTAL);
				db.getAllItems();
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo "+value+" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
						updateDb(p, value, pref.isBootChecked());
					}
				} );
				AlertDialog dialog = builder.create();
				dialog.show();
				dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
				Window window = dialog.getWindow();
				window.setLayout(800, LayoutParams.WRAP_CONTENT);
				return true;
			}

		});
	}



	private void updateDb(final Preference p, final String value,final boolean isChecked) {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				if(isChecked) {
					List<DataItem> items = db.getAllItems();
					for(DataItem item : items) {
						if(item.getName().equals("'"+p.getKey()+"'")) {
							db.deleteItemByName("'"+p.getKey()+"'");
						}
					}
					db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
				} else {
					if(db.getContactsCount() != 0) {
						db.deleteItemByName("'"+p.getKey()+"'");
					}
				}

				return "Executed";
			}
			@Override
			protected void onPostExecute(String result) {

			}
		}
		new LongOperation().execute();
	}
	
	private void updateListDb(final Preference p, final String value, final boolean isChecked) {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				if(isChecked) {
					List<DataItem> items = db.getAllItems();
					for(DataItem item : items) {
						if(item.getName().equals("'"+p.getKey()+"'")) {
							db.deleteItemByName("'"+p.getKey()+"'");
						}
					}
					db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
				} else {
					if(db.getContactsCount() != 0) {
						db.deleteItemByName("'"+p.getKey()+"'");
					}
				}

				return "Executed";
			}
			@Override
			protected void onPostExecute(String result) {

			}
		}
		new LongOperation().execute();
	}

}


