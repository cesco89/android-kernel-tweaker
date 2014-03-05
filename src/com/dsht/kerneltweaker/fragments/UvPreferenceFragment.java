package com.dsht.kerneltweaker.fragments;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

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
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.dsht.kerneltweaker.CustomPreference;
import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

public class UvPreferenceFragment extends PreferenceFragment {

	private PreferenceCategory mCategory;
	private Context mContext;
	private String[] names;
	private String[] values;
	private LinearLayout mButtonLayout;
	private Button mButtonApply;
	private Button mButtonCancel;
	private String UV_TABLE_FILE = "/sys/devices/system/cpu/cpu0/cpufreq/UV_mV_table";
	private String category = "uv";
	private DatabaseHandler db = MainActivity.db;
	private VddDatabaseHandler VddDb = MainActivity.vddDb;
	private List<DataItem> items;
	private List<DataItem> vddItems;
	private MenuItem boot;
	private boolean isVdd = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		init();
		items = db.getAllItems();
		vddItems = VddDb.getAllItems();

		if(Helpers.UvTableExists(UV_TABLE_FILE)) {
			if(mCategory.getPreferenceCount() != 0) {
				mCategory.removeAll();
			}
			MainActivity.menu.setEnabled(false);
			addPreferences(true);
			isVdd = false;
		} else {
			if(Helpers.UvTableExists("/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels")) {
				if(mCategory.getPreferenceCount() != 0) {
					mCategory.removeAll();
				}
				MainActivity.menu.setEnabled(false);
				UV_TABLE_FILE = "/sys/devices/system/cpu/cpufreq/vdd_table/vdd_levels";
				addPreferences(false);
				isVdd = true;
			} else {
				if(mCategory.getPreferenceCount() != 0) {
					mCategory.removeAll();
				}
				CustomPreference pref = new CustomPreference(mContext, true, category);
				pref.setTitle("Your Device Doesn\'t support UV");
				pref.hideBoot(true);
				pref.setSummary("You need a custom kernel that supports UnderVolt");
				pref.setTitleColor("#ff4444");
				pref.setSummaryColor("#ff4444");
				mCategory.addPreference(pref);
				if(MainActivity.menu.isMenuShowing()) {
					MainActivity.menu.toggle();
				}
			}

		}
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);

		final ListView list = (ListView) v.findViewById(android.R.id.list);

		mButtonLayout = (LinearLayout) v.findViewById(R.id.btn_layout);
		mButtonApply = (Button) v.findViewById(R.id.btn_apply);
		mButtonCancel = (Button) v.findViewById(R.id.btn_cancel);

		mButtonCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				for (int i = 0; i < mCategory.getPreferenceCount(); i++) {
					CustomPreference pref = (CustomPreference) mCategory.getPreference(i);
					pref.restoreSummaryKey(values[i], values[i]);
				}
				mButtonLayout.setVisibility(View.GONE);
				list.bringToFront();
			}

		});

		mButtonApply.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(isVdd) {
					for(int i = 0; i<mCategory.getPreferenceCount(); i++) {
						CustomPreference pref = (CustomPreference) mCategory.getPreference(i);
						String value = "'"+pref.getTitle().toString()+" "+pref.getSummary().toString()+"'";
						applyVddUV(value);
						Log.d("VALUE", value);
					}


				} else {
					String[] newValues = new String[values.length];
					for (int i = 0; i < mCategory.getPreferenceCount(); i++) {
						CustomPreference pref = (CustomPreference) mCategory.getPreference(i);
						newValues[i] = pref.getKey();
						values[i] = pref.getKey();

					}
					CMDProcessor.runSuCommand("echo \""+buildTable(values)+"\" > "+UV_TABLE_FILE);
					if(boot.isChecked()) {
						db.deleteItemByName("'"+UV_TABLE_FILE+"'");
						db.addItem(new DataItem("'"+UV_TABLE_FILE+"'", 
								buildTable(values), 
								"UV Table",
								category));
					}
				}
				mButtonLayout.setVisibility(View.GONE);
				list.bringToFront();
			}

		});
		return v;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		if(Helpers.UvTableExists(UV_TABLE_FILE)) {
			inflater.inflate(R.menu.menu_uv, menu);
			boot = (MenuItem) menu.findItem(R.id.action_boot);
			if(isVdd) {
				if(vddItems.size() != 0) {
					boot.setChecked(true);
				}else {
					boot.setChecked(false);
				}
			} else {
				for (DataItem item : items) {
					if(item.getName().contains(UV_TABLE_FILE)) {
						boot.setChecked(true);
						break;
					}
					boot.setChecked(false);
				}
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int prefsIndex = mCategory.getPreferenceCount();
		switch (item.getItemId()) {
		case R.id.action_plus:
			for (int i = 0; i< prefsIndex; i++) {
				CustomPreference pref = (CustomPreference) mCategory.getPreference(i);
				if(isVdd) {
					pref.setCustomSummaryKeyPlus(25000);
				} else {
					pref.setCustomSummaryKeyPlus(25);
				}
				if (!pref.getKey().equals(values[i])) {
					mButtonLayout.setVisibility(View.VISIBLE);
				} else {
					mButtonLayout.setVisibility(View.GONE);
				}
			}
			return true;
		case R.id.action_minus:
			for (int i = 0; i< prefsIndex; i++) {
				CustomPreference pref = (CustomPreference) mCategory.getPreference(i);
				if(isVdd) {
					pref.setCustomSummaryKeyMinus(25000);
				}else {
					pref.setCustomSummaryKeyMinus(25);
				}
				if (!pref.getKey().equals(values[i])) {
					mButtonLayout.setVisibility(View.VISIBLE);
				} else {
					mButtonLayout.setVisibility(View.GONE);
				}
			}
			return true;
		case R.id.action_boot:
			if(item.isChecked()) {
				if(isVdd) {
					VddDb.deleteAllItems();
				} else {
					db.deleteItemByName("'"+UV_TABLE_FILE+"'");
				}
				item.setChecked(false);
			}else {
				if(isVdd) {
					addVddBoot();
				} else {
					db.deleteItemByName("'"+UV_TABLE_FILE+"'");
					db.addItem(new DataItem("'"+UV_TABLE_FILE+"'", 
							buildTable(values), 
							"UV Table",
							category));
				}
				item.setChecked(true);
			}
			return true;
		default:
			break;
		}

		return false;
	}

	public void addPreferences(final boolean millivolts) {

		class LongOperation extends AsyncTask<String, Void, String> {

			@Override
			protected String doInBackground(String... params) {

				names = Helpers.getUvTableNames();
				values = Helpers.getUvValues();
				Log.d("table", buildTable(values));
				for(int i = 0; i<names.length; i++) {
					String name = names[i];
					final int j = i;
					CustomPreference pref = new CustomPreference(mContext, false, category);
					pref.setTitle(name);
					pref.areMilliVolts(millivolts);
					pref.hideBoot(true);
					String color = "";
					if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
						int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#ff0099cc"));
						color = "#"+Integer.toHexString(col);
					}else if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
						int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_UV, Color.parseColor("#ff0099cc"));
						color = "#"+Integer.toHexString(col);
					} 
					else {
						color = getResources().getStringArray(R.array.menu_colors)[4];
					}
					pref.setTitleColor(color);
					if(isVdd){
						pref.setSummary(values[i]);
					}else {
						pref.setSummary(values[i]+ " mV");
					}
					pref.setKey(values[i]);
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
							final String val = p.getKey().toString();
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
									if(isVdd) {
										String value = "'"+p.getTitle().toString()+" "+et.getText().toString()+"'";
										CMDProcessor.runSuCommand("echo "+value+" > "+UV_TABLE_FILE);
										p.setSummary(et.getText().toString());
										p.setKey(et.getText().toString());
									} else {
										String value = et.getText().toString();
										p.setSummary(value+" mV");
										p.setKey(value);
										values[j] = value;
										CMDProcessor.runSuCommand("echo \""+buildTable(values)+"\" > "+UV_TABLE_FILE);
									}
									if(boot.isChecked()) {
										if(isVdd) {
											addVddBoot();
										} else {
											db.deleteItemByName("'"+UV_TABLE_FILE+"'");
											db.addItem(new DataItem("'"+UV_TABLE_FILE+"'", 
													buildTable(values), 
													"UV Table",
													category));
										}
									}

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
				Helpers.waitForMillis(500, mContext);
				MainActivity.menu.setEnabled(true);
				if(MainActivity.menu.isMenuShowing()) {
					MainActivity.menu.toggle();
				}

			}

			@Override
			protected void onPreExecute() {

			}


		}
		new LongOperation().execute();
	}

	private String buildTable(String[] vals) {
		String newTable="";
		for(int j = 0; j<vals.length; j++) {
			if(j!= vals.length-1) {
				newTable+=vals[j]+" ";
			}else{
				newTable+=vals[j];
			}
		}
		return newTable;
	}

	public void init() {
		addPreferencesFromResource(R.xml.pref_sccreen_uv);
		mContext = getActivity();
		mCategory = (PreferenceCategory) findPreference("key_uv_category");
	}

	private void applyVddUV(String value) {
		CMDProcessor.runSuCommand("echo "+value+" > "+UV_TABLE_FILE);
	}

	private void addVddBoot() {
		VddDb.deleteAllItems();
		for(int i = 0; i<mCategory.getPreferenceCount(); i++) {
			CustomPreference pref = (CustomPreference)mCategory.getPreference(i);
			String value = "'"+pref.getTitle().toString()+" "+pref.getSummary().toString()+"'";
			VddDb.addItem(new DataItem("'"+UV_TABLE_FILE+"'", value, "vdd_levels", category));
		}
	}

}
