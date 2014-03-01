package com.dsht.settings;

import java.io.File;

import it.gmariotti.android.example.colorpicker.calendarstock.ColorPickerPreference;

import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.Startup;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsFragment extends PreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener {

	public static final String KEY_ENABLE_GLOBAL = "key_enable_global";
	public static final String KEY_ENABLE_PERSONAL = "key_enable_personal";
	public static final String KEY_GLOBAL_COLOR = "key_global_color";
	public static final String KEY_THEME = "key_theme";
	public static final String KEY_STAT = "key_color_stats";
	public static final String KEY_INFO = "key_color_info";
	public static final String KEY_CPU = "key_color_cpu";
	public static final String KEY_GPU = "key_color_gpu";
	public static final String KEY_UV = "key_color_uv";
	public static final String KEY_KERNEL = "key_color_kernel";
	public static final String KEY_LMK = "key_color_lmk";
	public static final String KEY_VM = "key_color_vm";
	public static final String KEY_REVIEW = "key_color_review";
	public static final String KEY_FILE = "key_color_file";
	public static final String KEY_BAK = "key_color_backup";
	public static final String KEY_RECOVERY = "key_color_recovery";
	public static final String KEY_PERSONAL_CAT = "key_personal_category";
	public static final String KEY_PROP = "key_color_prop";
	public static final String KEY_INIT = "key_color_init";
	public static final String KEY_BLUR = "key_color_blur";
	public static final String KEY_DEBUG = "key_debug";
	public static final String KEY_SLOG = "key_slog";
	public static final String LOG_FILE = Environment.getExternalStorageDirectory().getAbsolutePath()+"/KernelTweaker_log.txt";
	public static final String KEY_RUNLOG = "key_runlog";
	
	private CheckBoxPreference mEnable;
	private CheckBoxPreference mPersonal;
	private ColorPickerPreference mColor;
	private ColorPickerPreference mStats;
	private ColorPickerPreference mInfo;
	private ColorPickerPreference mCpu;
	private ColorPickerPreference mGpu;
	private ColorPickerPreference mUv;
	private ColorPickerPreference mKernel;
	private ColorPickerPreference mLmk;
	private ColorPickerPreference mVm;
	private ColorPickerPreference mReview;
	private ColorPickerPreference mFile;
	private ColorPickerPreference mBak;
	private ColorPickerPreference mRecovery;
	private ColorPickerPreference mProp;
	private ColorPickerPreference mInit;
	private ColorPickerPreference mBlur;
	private CheckBoxPreference mTheme;
	private SharedPreferences mPrefs;
	private PreferenceCategory mPersonalCat;
	private Preference mLog;
	private Preference mRunLog;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_settings);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mEnable = (CheckBoxPreference) findPreference(KEY_ENABLE_GLOBAL);
		mPersonal = (CheckBoxPreference) findPreference(KEY_ENABLE_PERSONAL);
		mTheme = (CheckBoxPreference) findPreference(KEY_THEME);
		mColor = (ColorPickerPreference) findPreference(KEY_GLOBAL_COLOR);
		mStats = (ColorPickerPreference) findPreference(KEY_STAT);
		mInfo = (ColorPickerPreference) findPreference(KEY_INFO);
		mCpu = (ColorPickerPreference) findPreference(KEY_CPU);
		mGpu = (ColorPickerPreference) findPreference(KEY_GPU);
		mUv = (ColorPickerPreference) findPreference(KEY_UV);
		mKernel = (ColorPickerPreference) findPreference(KEY_KERNEL);
		mLmk = (ColorPickerPreference) findPreference(KEY_LMK);
		mVm = (ColorPickerPreference) findPreference(KEY_VM);
		mReview = (ColorPickerPreference) findPreference(KEY_REVIEW);
		mFile = (ColorPickerPreference) findPreference(KEY_FILE);
		mBak = (ColorPickerPreference) findPreference(KEY_BAK);
		mRecovery = (ColorPickerPreference) findPreference(KEY_RECOVERY);

		mProp = (ColorPickerPreference) findPreference(KEY_PROP);
		mInit = (ColorPickerPreference) findPreference(KEY_INIT);
		mBlur = (ColorPickerPreference) findPreference(KEY_BLUR);

		mPersonalCat = (PreferenceCategory) findPreference(KEY_PERSONAL_CAT);

		mLog = (Preference) findPreference(KEY_SLOG);
		mRunLog = (Preference) findPreference(KEY_RUNLOG);


		boolean enabled = mPrefs.getBoolean(KEY_ENABLE_GLOBAL, false);
		mColor.setEnabled(enabled);

		mEnable.setOnPreferenceChangeListener(this);
		mTheme.setOnPreferenceChangeListener(this);
		mColor.setOnPreferenceChangeListener(this);
		mPersonal.setOnPreferenceChangeListener(this);
		mStats.setOnPreferenceChangeListener(this);;
		mInfo.setOnPreferenceChangeListener(this);
		mCpu.setOnPreferenceChangeListener(this);
		mGpu.setOnPreferenceChangeListener(this);
		mUv.setOnPreferenceChangeListener(this);
		mKernel.setOnPreferenceChangeListener(this);
		mLmk.setOnPreferenceChangeListener(this);
		mVm.setOnPreferenceChangeListener(this);
		mReview.setOnPreferenceChangeListener(this);
		mFile.setOnPreferenceChangeListener(this);
		mBak.setOnPreferenceChangeListener(this);
		mRecovery.setOnPreferenceChangeListener(this);
		mProp.setOnPreferenceChangeListener(this);
		mInit.setOnPreferenceChangeListener(this);
		mBlur.setOnPreferenceChangeListener(this);
		mLog.setOnPreferenceClickListener(this);
		mRunLog.setOnPreferenceClickListener(this);

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}

		if(!mPrefs.getBoolean(KEY_ENABLE_PERSONAL, false)) {
			this.getPreferenceScreen().removePreference(mPersonalCat);
		}


	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor = mPrefs.edit();
		if(pref == mEnable) {
			editor.putBoolean(KEY_ENABLE_GLOBAL, (Boolean)newValue);
			mColor.setEnabled((Boolean)newValue);
			MainActivity.mAdapter.notifyDataSetChanged();
			if(mPersonal.isChecked()) {
				mPersonal.setChecked(false);
			}
			return true;
		}
		if(pref == mPersonal) {
			editor.putBoolean(KEY_ENABLE_PERSONAL, (Boolean)newValue);
			MainActivity.mAdapter.notifyDataSetChanged();
			if(mEnable.isChecked()) {
				mEnable.setChecked(false);
			}
			if(!(Boolean)newValue) {
				this.getPreferenceScreen().removePreference(mPersonalCat);
			}else {
				this.getPreferenceScreen().addPreference(mPersonalCat);
			}
			return true;
		}
		if(pref == mColor) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mTheme) {
			editor.putBoolean(KEY_THEME, (Boolean)newValue);
			MainActivity.setAppTheme();
			Helpers.restartPC(getActivity());
			return true;
		}
		if(pref == mStats) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mInfo) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mCpu) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mGpu) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mUv) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mKernel) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mLmk) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mVm) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mReview) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mFile) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mBak) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mRecovery) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mProp) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mInit) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		if(pref == mBlur) {
			MainActivity.mAdapter.notifyDataSetChanged();
			return true;
		}
		editor.commit();
		return false;
	}

	private void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Info");
		builder.setMessage("KernelTweaker needs to be restarted to apply changes.\nRestart now?");
		builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Helpers.restartPC(getActivity());
			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	@Override
	public boolean onPreferenceClick(Preference pref) {
		// TODO Auto-generated method stub
		if(pref == mLog) {
			if(new File(LOG_FILE).exists()) {
				String fcontent = Helpers.readFileViaShell(LOG_FILE, false);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View v = inflater.inflate(R.layout.log_dialog, null, false);
				TextView textLog = (TextView)v.findViewById(R.id.text);
				textLog.setText(fcontent);
				builder.setTitle("Log content");
				builder.setView(v);
				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				});
				builder.setNegativeButton("Delete Log", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						new File(LOG_FILE).delete();
						dialog.cancel();
						
					}
				});
				builder.create().show();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle(null);
				builder.setMessage("Log File not found!\nPlease enable debugging and reboot your phone!");
				builder.create().show();
			}
		}
		if(pref == mRunLog) {
			Startup.applyValuesAsync(getActivity(), MainActivity.db, MainActivity.vddDb,true);
		}
		return false;
	}

}
