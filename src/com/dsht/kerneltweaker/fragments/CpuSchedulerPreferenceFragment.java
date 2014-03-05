package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

public class CpuSchedulerPreferenceFragment extends PreferenceFragment {

	private static PreferenceCategory mCategory;
	private static Context mContext;
	private PreferenceScreen mRoot;
	private static String category = "scheduler";
	private static DatabaseHandler db = MainActivity.db;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_scheduler);
		mRoot = (PreferenceScreen) findPreference("key_root");
		mCategory = (PreferenceCategory) findPreference("key_sched_category");
		mContext = getActivity();
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);
		
		File f = new File("/sys/block/mmcblk0/queue/iosched");
		String curSched = Helpers.getCurrentScheduler();
		File[] files = f.listFiles();
		if(files.length != 0) {
			MainActivity.menu.setEnabled(false);
			addPreferences();
			mCategory.setTitle(curSched + " Tweakable values");
		}else {
			Preference pref = new Preference(mContext);
			pref.setTitle("No tweakable values");
			pref.setSummary(curSched + " Doesn\'t have tweakable values");
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

				File f = new File("/sys/block/mmcblk0/queue/iosched");
				File[] files = f.listFiles();
				for(File file : files) {
					String fileName = file.getName();
					String filePath = file.getAbsolutePath();
					final String fileContent = Helpers.getFileContent(file);
					CustomPreference pref = new CustomPreference(mContext, true, category);
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
									//Log.d("TEST", "echo "+value+" > "+ p.getKey());
									CMDProcessor.runSuCommand("echo "+value+" > "+p.getKey());
									updateListDb(p, value, ((CustomPreference) p).isBootChecked());
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
