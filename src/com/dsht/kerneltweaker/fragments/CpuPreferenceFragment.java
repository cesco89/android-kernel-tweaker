package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.CustomListPreference;
import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.ListViewMultiChoiceModeListener;
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
import android.app.Fragment;
import android.app.FragmentTransaction;
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
import android.widget.ListView;

public class CpuPreferenceFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {
	private Context mContext;
	private SharedPreferences mPrefs;
	private CustomListPreference mCpuMaxFreq;
	private CustomListPreference mCpuMinFreq;
	private CustomListPreference mCpuGovernor;
	private CustomPreference mAdvancedGovernor;
	private PreferenceCategory mHotPlugCategory;
	private CustomListPreference mCpuquiet;
	private CustomPreference mAdvancedCpuquiet;
	private PreferenceCategory mAdvancedCategory;
	private PreferenceScreen mRoot;
	private static final String MAX_FREQ_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_max_freq";
	private static final String GOVERNOR_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor";
	private static final String MIN_FREQ_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_min_freq";
	private static final String HOTPLUG_FILE ="/sys/devices/virtual/misc/mako_hotplug_control/"; 
	private static final String CPUQUIET_DIR = "/sys/devices/system/cpu/cpuquiet";
	private static final String CPUQUIET_FILE = "/sys/devices/system/cpu/cpuquiet/current_governor";
	private static final String CPUQUIET_GOVERNORS = "/sys/devices/system/cpu/cpuquiet/available_governors";
	private static final String category = "cpu";
	private DatabaseHandler db = MainActivity.db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_cpu);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle();
		}
		RootTools.isRootAvailable();

		Helpers.setPermissions(MAX_FREQ_FILE);
		Helpers.setPermissions(MIN_FREQ_FILE);
		Helpers.setPermissions(GOVERNOR_FILE);
		Helpers.setPermissions(HOTPLUG_FILE);
		Helpers.setPermissions(CPUQUIET_FILE);
		Helpers.setPermissions(CPUQUIET_DIR);
		Helpers.setPermissions(CPUQUIET_GOVERNORS);

		mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mHotPlugCategory = (PreferenceCategory) findPreference("key_hotplug_category");
		mCpuMaxFreq = (CustomListPreference) findPreference("key_cpu_max");
		mCpuMinFreq = (CustomListPreference) findPreference("key_cpu_min");
		mCpuGovernor = (CustomListPreference) findPreference("key_cpu_governor");
		mAdvancedGovernor = (CustomPreference) findPreference("key_advanced_governor");
		mAdvancedCpuquiet = (CustomPreference) findPreference("key_advanced_cpuquiet");
		mCpuquiet = (CustomListPreference) findPreference("key_cpuquiet");
		mAdvancedCategory = (PreferenceCategory) findPreference("key_advanced");
		mAdvancedGovernor.setOnPreferenceClickListener(this);
		mAdvancedCpuquiet.setOnPreferenceClickListener(this);

		if(!new File(CPUQUIET_DIR).exists()) {
			mAdvancedCategory.removePreference(mAdvancedCpuquiet);
		}

		String color = "";
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			color = "#"+Integer.toHexString(col);
		}else if(mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_CPU, Color.parseColor("#ff0099cc"));
			color = "#"+Integer.toHexString(col);
		}
		else {
			String col = mContext.getResources().getStringArray(R.array.menu_colors)[2];
			color = col;
		}
		mCpuMaxFreq.setTitleColor(color);
		mCpuMinFreq.setTitleColor(color);
		mCpuGovernor.setTitleColor(color);
		mAdvancedGovernor.setTitleColor(color);
		mCpuquiet.setTitleColor(color);
		mAdvancedCpuquiet.setTitleColor(color);

		mCpuMaxFreq.setCategory(category);
		mCpuMinFreq.setCategory(category);
		mCpuGovernor.setCategory(category);
		mCpuquiet.setCategory(category);

		mCpuMaxFreq.setKey(MAX_FREQ_FILE);
		mCpuMinFreq.setKey(MIN_FREQ_FILE);
		mCpuGovernor.setKey(GOVERNOR_FILE);
		mCpuquiet.setKey(CPUQUIET_FILE);


		String[] frequencies = Helpers.getFrequencies();
		String[] governors = Helpers.getGovernors();
		String[] names = Helpers.getFrequenciesNames();

		mCpuMaxFreq.setEntries(names);
		mCpuMaxFreq.setEntryValues(frequencies);
		mCpuMinFreq.setEntries(names);
		mCpuMinFreq.setEntryValues(frequencies);
		mCpuGovernor.setEntries(governors);
		mCpuGovernor.setEntryValues(governors);

		if(new File(CPUQUIET_DIR).exists()) {
			String cpuquiet = Helpers.getFileContent(new File(CPUQUIET_GOVERNORS));
			String[] cpuquiet_govs = cpuquiet.trim().replaceAll("\n", "").split(" ");
			String currQuiet = Helpers.getFileContent(new File(CPUQUIET_FILE)).trim().replace("\n", "");
			mCpuquiet.setSummary(currQuiet);
			mCpuquiet.setEntries(cpuquiet_govs);
			mCpuquiet.setEntryValues(cpuquiet_govs);
			mCpuquiet.setValue(currQuiet);

		} else {
			mHotPlugCategory.removePreference(mCpuquiet);
		}


		if(new File(MAX_FREQ_FILE).exists()) {
			mCpuMaxFreq.setSummary(Helpers.readOneLine(MAX_FREQ_FILE));
			mCpuMaxFreq.setValue(mCpuMaxFreq.getSummary().toString());
		}
		if(new File(MIN_FREQ_FILE).exists()) {
			mCpuMinFreq.setSummary(Helpers.readOneLine(MIN_FREQ_FILE));
			mCpuMinFreq.setValue(mCpuMinFreq.getSummary().toString());
		}
		if(new File(GOVERNOR_FILE).exists()) {
			Helpers.runRootCommand("chmod 655 /sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
			mCpuGovernor.setSummary(Helpers.getCurrentGovernor());
			mCpuGovernor.setValue(mCpuGovernor.getSummary().toString());
		}

		mCpuMaxFreq.setOnPreferenceChangeListener(this);
		mCpuMinFreq.setOnPreferenceChangeListener(this);
		mCpuGovernor.setOnPreferenceChangeListener(this);
		mAdvancedGovernor.excludeFromDialog(true);
		mCpuquiet.setOnPreferenceChangeListener(this);

		if(new File(HOTPLUG_FILE).exists()) {
			createPreference(mHotPlugCategory,new File(HOTPLUG_FILE+"cores_on_touch"), color );
			createPreference(mHotPlugCategory,new File(HOTPLUG_FILE+"first_level"), color );
		} 

		if(mHotPlugCategory.getPreferenceCount() == 0) {
			mRoot.removePreference(mHotPlugCategory);
		}

		mAdvancedCpuquiet.hideBoot(true);
		mAdvancedGovernor.hideBoot(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);
		
		return v;
	}



	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		if(pref == mCpuMaxFreq) {
			String value = (String) newValue;
			mCpuMaxFreq.setSummary(value);
			mCpuMaxFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MAX_FREQ_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuMinFreq) {
			String value = (String) newValue;
			mCpuMinFreq.setSummary(value);
			mCpuMinFreq.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+MIN_FREQ_FILE);
			updateListDb(pref, value,((CustomListPreference) pref).isBootChecked());
		}
		if(pref == mCpuGovernor) {
			String value = ((String)newValue).trim().replaceAll(" ", "").replaceAll("\n", "");
			mCpuGovernor.setSummary(value);
			mCpuGovernor.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+GOVERNOR_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());

		}
		if(pref == mCpuquiet) {
			String value = (String) newValue;
			mCpuquiet.setSummary(value);
			mCpuquiet.setValue(value);
			CMDProcessor.runSuCommand("echo "+value+" > "+CPUQUIET_FILE);
			updateListDb(pref, value, ((CustomListPreference) pref).isBootChecked());
		}
		return false;
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		// TODO Auto-generated method stub
		Fragment f = null;
		if(pref == mAdvancedGovernor) {
			f = new CpuGovernorPreferenceFragment();
		}
		if(pref == mAdvancedCpuquiet) {
			f = new CpuquietGovernorPreferenceFragment();
		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// This adds the newly created Preference fragment to my main layout, shown below
		ft.replace(R.id.activity_container,f);
		// By hiding the main fragment, transparency isn't an issue
		ft.addToBackStack("TAG");
		ft.commit();


		return false;
	}


	private void createPreference(PreferenceCategory mCategory, File file, String color) {
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
				builder.setView(v);
				builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String value = et.getText().toString();
						p.setSummary(value);
						Log.d("TEST", "echo "+value+" > "+ p.getKey());
						CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
						updateListDb(pref, value, pref.isBootChecked());
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
