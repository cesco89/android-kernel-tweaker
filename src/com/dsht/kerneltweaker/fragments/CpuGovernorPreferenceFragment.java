package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.glossary.ConservativeGlossaryFragment;
import com.dsht.glossary.CpuGlossaryFragment;
import com.dsht.glossary.InteractiveGlossaryFragment;
import com.dsht.glossary.OndemandGlossaryFragment;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
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
import android.widget.LinearLayout;
import android.widget.ListView;

public class CpuGovernorPreferenceFragment extends PreferenceFragment {

	private static PreferenceCategory mCategory;
	private static Context mContext;
	private static final String category = "governor";
	private PreferenceScreen mRoot;
	private static DatabaseHandler db = MainActivity.db;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_governor);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mCategory = (PreferenceCategory) findPreference("key_gov_category");
		mContext = getActivity();
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);
		
		String curGov = Helpers.getCurrentGovernor();
		
		File f = new File("/sys/devices/system/cpu/cpufreq/"+curGov);
		if(f.exists()) {
			MainActivity.menu.setEnabled(false);
			mCategory.setTitle(curGov + " Tweakable values");
			addPreferences();

			if(curGov.equalsIgnoreCase("ondemand")||
					curGov.equalsIgnoreCase("interactive") ||
					curGov.equalsIgnoreCase("conservative")) {
				Fragment glo = null;
				if(curGov.equalsIgnoreCase("ondemand")) {
					glo = new OndemandGlossaryFragment();
				}else if( curGov.equalsIgnoreCase("interactive")) {
					glo = new InteractiveGlossaryFragment();
				}else if( curGov.equalsIgnoreCase("conservative")) {
					glo = new ConservativeGlossaryFragment();
				}
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				// This adds the newly created Preference fragment to my main layout, shown below
				ft.replace(R.id.menu_frame, glo);
				// By hiding the main fragment, transparency isn't an issue
				ft.addToBackStack("TAG");
				ft.commit();
			}
		}
		else {
			CustomPreference pref = new CustomPreference(mContext, true, category);
			pref.setTitle("No tweakable values");
			pref.setSummary(curGov + " Doesn\'t have tweakable values");
			pref.setTitleColor("#ff4444");
			pref.setSummaryColor("#ff4444");
			mCategory.addPreference(pref);
		}



		return v;	
	}

	public static void addPreferences() {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {
				if(mCategory.getPreferenceCount() != 0) {
					mCategory.removeAll();
				}
				String currentGovernor = Helpers.getCurrentGovernor();
				File f = new File("/sys/devices/system/cpu/cpufreq/"+currentGovernor);
				if(f.exists()) {
					File[] files = f.listFiles();
					for(File file : files) {
						String fileName = file.getName();
						String filePath = file.getAbsolutePath();
						Helpers.runRootCommand("chmod 655 "+file.getAbsolutePath());
						final String fileContent = Helpers.readFileViaShell(filePath, false).trim().replaceAll("\n", "");
						CustomPreference pref = new CustomPreference(mContext,true,category);
						pref.setTitle(fileName);
						String color = "#ff0099cc";
						if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
							int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#ff0099cc"));
							color = "#"+Integer.toHexString(col);
							pref.setTitleColor(color);
						}else {
							pref.setTitleColor(color);
						}
						pref.setSummary(fileContent);
						pref.setKey(filePath);
						Log.d("CONTENT", fileContent);
						mCategory.addPreference(pref);
						pref.setOnPreferenceClickListener(new OnPreferenceClickListener(){

							@Override
							public boolean onPreferenceClick(final Preference p) {
								// TODO Auto-generated method stub
								AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
								LinearLayout ll = new LinearLayout(mContext);
								ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
								final EditText et = new EditText(mContext);
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
								params.setMargins(40, 40, 40, 40);
								params.gravity = Gravity.CENTER;
								String val = p.getSummary().toString();
								et.setLayoutParams(params);
								et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
								et.setGravity(Gravity.CENTER_HORIZONTAL);
								et.setText(val);
								ll.addView(et);
								builder.setView(ll);
								builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										String value = et.getText().toString();
										p.setSummary(value);
										CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
										updateListDb(p,value, ((CustomPreference) p).isBootChecked());
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
				}

				return "Executed";
			}

			@Override
			protected void onPostExecute(String result) {

			}

			@Override
			protected void onPreExecute() {}

		}
		new LongOperation().execute(); 
	}

	private static void updateListDb(final Preference p, final String value, final boolean isChecked) {

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
