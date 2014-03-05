package com.dsht.kerneltweaker.fragments;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;


public class InitD extends PreferenceFragment implements OnPreferenceChangeListener {

	private static final String KEY_ZIPALIGN_APKS = "zipalign_apks";
	private static final String KEY_FIX_PERMISSIONS = "fix_permissions";
	private static final String KEY_CLEAR_DATA_CACHE = "clear_data_cache";
	private static final String KEY_ENABLE_CRON = "enable_cron";
	private static final String KEY_FILE_SYSTEM_SPEEDUPS = "file_system_speedups";
	private static final String REMOUNT_CMD = "busybox mount -o %s,remount /dev/block/mmcblk0p1 /system";

	private static final String[] KEYS = {
		KEY_ZIPALIGN_APKS, //0
		KEY_FIX_PERMISSIONS, //1
		KEY_CLEAR_DATA_CACHE,  //2
		KEY_ENABLE_CRON, //3
		KEY_FILE_SYSTEM_SPEEDUPS, //4
	};

	protected SharedPreferences mPrefs;
	private CheckBoxPreference mZipAlign;
	private CheckBoxPreference mFixPermissions;
	private CheckBoxPreference mClearCache;
	private CheckBoxPreference mEnableCron;
	private CheckBoxPreference mSysSpeedup;

	private static InitD sActivity;
	private static final String SCRIPT_HEAD = "#!/system/bin/sh";
	private static final String SCRIPT_HELPERS = ". /system/etc/helpers.sh";
	private static final String SCRIPT_PERMS = "chmod 755";
	private static final String ZIPALIGN_FILE = "01zipalign";
	private static final String INIT_PATH = "/system/etc/init.d/";
	private static final String CLEAR_CACHE_COMMAND = "busybox find /data/data -type d -iname \"*cache*\" -maxdepth 2 -mindepth 2 -exec busybox rm -rf {} ';' ";
	private static final String CACHE_FILE = "06removecache";
	private static final String FIXPERMS_FILE = "07fixperms";
	private static final String CRON_FILE = "09cron";
	private static final String TWEAKS_FILE = "98tweaks";

	public static InitD whatActivity() {
		return sActivity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sActivity = this;
		mPrefs    = PreferenceManager.getDefaultSharedPreferences(getActivity());
		addPreferencesFromResource(R.xml.init_d);

		mZipAlign = (CheckBoxPreference) findPreference(KEYS[0]);
		mFixPermissions = (CheckBoxPreference) findPreference(KEYS[1]);
		mClearCache = (CheckBoxPreference) findPreference(KEYS[2]);
		mEnableCron = (CheckBoxPreference) findPreference(KEYS[3]);
		mSysSpeedup = (CheckBoxPreference) findPreference(KEYS[4]);

		mZipAlign.setOnPreferenceChangeListener(this);
		mFixPermissions.setOnPreferenceChangeListener(this);
		mClearCache.setOnPreferenceChangeListener(this);
		mEnableCron.setOnPreferenceChangeListener(this);
		mSysSpeedup.setOnPreferenceChangeListener(this);

		loadValues();
		copyHelpers();
		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
	}

	@Override
	public boolean onPreferenceChange(Preference pref, Object newValue) {
		// TODO Auto-generated method stub
		boolean value = (Boolean)newValue;
		if(pref == mZipAlign) {
			if(value) {
				createInitD(ZIPALIGN_FILE, "zipalign_apks");
			} else {
				deleteInitD(INIT_PATH+ZIPALIGN_FILE);
			}
			return true;
		}
		if(pref == mFixPermissions) {
			if(value) {
				createInitD(FIXPERMS_FILE,"fix_permissions");
			} else {
				deleteInitD(INIT_PATH+FIXPERMS_FILE);
			}
			return true;
		}
		if(pref == mClearCache) {
			if(value) {
				createInitD(CACHE_FILE, CLEAR_CACHE_COMMAND);
			}else {
				deleteInitD(INIT_PATH+CACHE_FILE);
			}
			return true;
		}
		if(pref == mEnableCron) {
			if(value) {
				copyScript(CRON_FILE);
			}else {
				deleteInitD(INIT_PATH+CRON_FILE);
			}
			return true;
		}
		if(pref == mSysSpeedup) {
			if(value) {
				copyScript(TWEAKS_FILE);
			}else {
				deleteInitD(INIT_PATH+TWEAKS_FILE);
			}
			return true;
		}
		loadValues();

		return false;
	}


	private void createInitD(final String filename, final String filecontent) {

		class AsyncCreateInitTask extends AsyncTask<Void, Void, Boolean> {

			ProgressDialog pd;

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(getActivity());
				pd.setIndeterminate(true);
				pd.setMessage("Creating script...Please wait");
				pd.setCancelable(false);
				pd.show();
			}

			@Override
			protected Boolean doInBackground(Void... params) {

				mount("rw");
				new CMDProcessor().su.runWaitFor("echo \""+SCRIPT_HEAD+"\" >> " + INIT_PATH + filename);
				new CMDProcessor().su.runWaitFor("echo \""+SCRIPT_HELPERS+"\" >> "+ INIT_PATH + filename );
				new CMDProcessor().su.runWaitFor("echo \""+ filecontent + "\" >> " + INIT_PATH + filename);
				new CMDProcessor().su.runWaitFor(SCRIPT_PERMS+" " + INIT_PATH + filename);
				mount("ro");
				return true;
			}

			@Override
			protected void onPostExecute(Boolean res) {
				// result holds what you return from doInBackground
				super.onPostExecute(res);
				pd.dismiss();
			}
		}
		new AsyncCreateInitTask().execute();
	}

	private void deleteInitD(final String filepath) {

		class AsyncDeleteInitTask extends AsyncTask<Void, Void, Boolean> {

			ProgressDialog pd;

			@Override
			protected void onPreExecute() {
				pd = new ProgressDialog(getActivity());
				pd.setIndeterminate(true);
				pd.setMessage("Deleting script...Please wait");
				pd.setCancelable(false);
				pd.show();
			}

			@Override
			protected Boolean doInBackground(Void... params) {

				mount("rw");
				new CMDProcessor().su.runWaitFor("rm -f "+filepath);
				mount("ro");
				return true;
			}
			@Override
			protected void onPostExecute(Boolean res) {
				// result holds what you return from doInBackground
				super.onPostExecute(res);
				pd.dismiss();
			}
		}
		new AsyncDeleteInitTask().execute();
	}


	private void loadValues() {
		if(new File(INIT_PATH+ZIPALIGN_FILE).exists()) {
			mZipAlign.setChecked(true);
		}
		if(new File(INIT_PATH+FIXPERMS_FILE).exists()) {
			mFixPermissions.setChecked(true);
		}
		if(new File(INIT_PATH+CACHE_FILE).exists()) {
			mClearCache.setChecked(true);
		}
		if(new File(INIT_PATH+CRON_FILE).exists()) {
			mEnableCron.setChecked(true);
		}
		if(new File(INIT_PATH+TWEAKS_FILE).exists()) {
			mSysSpeedup.setChecked(true);
		}
	}

	public boolean mount(String read_value) {
		Log.d("TAG", "Remounting /system " + read_value);
		return new CMDProcessor().su.runWaitFor(String.format(REMOUNT_CMD, read_value)).success();
	}

	private void copyHelpers() {
		if(!new File("/system/etc/helpers.sh").exists()) {
			class AsyncCopyHelpersTask extends AsyncTask<Void, Void, Boolean> {

				ProgressDialog pd;

				@Override
				protected void onPreExecute() {
					pd = new ProgressDialog(getActivity());
					pd.setIndeterminate(true);
					pd.setMessage("Copying Helpers...Please wait");
					pd.setCancelable(false);
					pd.show();
				}

				@Override
				protected Boolean doInBackground(Void... params) {


					mount("rw");
					new CMDProcessor().su.runWaitFor("cp /data/data/com.dsht.kerneltweaker/files/helpers.sh " + "/system/etc");
					new CMDProcessor().su.runWaitFor("chmod 644 /system/etc/helpers.sh");
					mount("ro");

					return true;
				}
				@Override
				protected void onPostExecute(Boolean res) {
					// result holds what you return from doInBackground
					super.onPostExecute(res);
					pd.dismiss();
				}
			}
			new AsyncCopyHelpersTask().execute();
		}
	}
	
	private void copyScript(final String name) {
			class AsyncCopyScriptTask extends AsyncTask<Void, Void, Boolean> {

				ProgressDialog pd;

				@Override
				protected void onPreExecute() {
					pd = new ProgressDialog(getActivity());
					pd.setIndeterminate(true);
					pd.setMessage("Copying script...Please wait");
					pd.setCancelable(false);
					pd.show();
				}

				@Override
				protected Boolean doInBackground(Void... params) {

					copyAsset(name);
					mount("rw");
					new CMDProcessor().su.runWaitFor("cp /data/data/com.dsht.kerneltweaker/files/"+name+" " + "/system/etc/init.d");
					new CMDProcessor().su.runWaitFor("chmod 644 /system/etc/init.d/"+name);
					mount("ro");

					return true;
				}
				@Override
				protected void onPostExecute(Boolean res) {
					// result holds what you return from doInBackground
					super.onPostExecute(res);
					pd.dismiss();
				}
			}
			new AsyncCopyScriptTask().execute();
	}
	
	private void copyAsset(String name) {

		if(!new File(getActivity().getFilesDir().getPath(),name).exists()) {

			InputStream stream = null;
			OutputStream output = null;

			try {
				stream = getActivity().getAssets().open(name);
				output = new BufferedOutputStream(new FileOutputStream(getActivity().getFilesDir()+"/"+name));

				byte data[] = new byte[1024];
				int count;

				while((count = stream.read(data)) != -1)
				{
					output.write(data, 0, count);
				}

				output.flush();
				output.close();
				stream.close();

				stream = null;
				output = null;

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

}
