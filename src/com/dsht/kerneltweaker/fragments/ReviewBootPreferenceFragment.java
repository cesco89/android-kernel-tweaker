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
import com.dsht.kerneltweaker.SwipeDismissListViewTouchListener;
import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;
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
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ListView;

public class ReviewBootPreferenceFragment extends PreferenceFragment {

	private DatabaseHandler db;
	private VddDatabaseHandler VddDb;
	private List<DataItem> items;
	private List<DataItem> vddItems;
	private PreferenceScreen mRoot;
	private Context mContext;
	private PreferenceCategory mCpu;
	private PreferenceCategory mGpu;
	private PreferenceCategory mUv;
	private PreferenceCategory mKernel;
	private PreferenceCategory mLmk;
	private PreferenceCategory mGov;
	private PreferenceCategory mSched;
	private PreferenceCategory mQuiet;
	private PreferenceCategory mVm;

	private static final String cpuCat = "cpu";
	private static final String gpuCat = "gpu";
	private static final String uvCat ="uv";
	private static final String kernelCat = "kernel";
	private static final String LmkCat = "lmk";
	private static final String GovCat = "governor";
	private static final String SchedCat ="scheduler";
	private static final String QuietCat ="cpuquiet";
	private static final String vmCat = "vm";

	private ListView listView;
	private SwipeDismissListViewTouchListener touchListener;
	private MenuItem edit;
	String[] frequencies;
	String[] names;
	String[] governors;
	String[] gpuFrequencies;
	String[] schedulers;
	String[] cpuquiet_govs;
	String[] availTCP;
	String[] readAheadKb = {"128","256","384","512","640","768","896","1024","1152",
			"1280","1408","1536","1664","1792","1920","2048", "2176", "2304", "2432", "2560", 
			"2688", "2816", "2944", "3072", "3200", "3328", "3456", "3584", "3712", "3840", "3968", "4096"};
	private static final String GPU_FREQUENCIES_FILE = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
	private static final String SCHEDULER_FILE = "/sys/block/mmcblk0/queue/scheduler";
	private static final String READ_AHEAD_FILE = "/sys/block/mmcblk0/queue/read_ahead_kb";
	private static final String CPUQUIET_DIR = "/sys/devices/system/cpu/cpuquiet";
	private static final String CPUQUIET_FILE = "/sys/devices/system/cpu/cpuquiet/current_governor";
	private static final String CPUQUIET_GOVERNORS = "/sys/devices/system/cpu/cpuquiet/available_governors";
	private static final String TCP_OPTIONS = "sysctl net.ipv4.tcp_available_congestion_control";


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref_screen_review);
		mRoot = (PreferenceScreen) findPreference("key_pref_screen");
		mContext = getActivity();
		mCpu = (PreferenceCategory) findPreference("cat_cpu");
		mGpu = (PreferenceCategory) findPreference("cat_gpu");
		mUv = (PreferenceCategory) findPreference("cat_uv");
		mKernel = (PreferenceCategory) findPreference("cat_kernel");
		mLmk = (PreferenceCategory) findPreference("cat_lmk");
		mGov = (PreferenceCategory) findPreference("cat_gov");
		mSched = (PreferenceCategory) findPreference("cat_sched");
		mQuiet = (PreferenceCategory) findPreference("cat_quiet");
		mVm= (PreferenceCategory) findPreference("cat_vm");
		setHasOptionsMenu(true);

		Helpers.setPermissions(CPUQUIET_FILE);
		Helpers.setPermissions(GPU_FREQUENCIES_FILE);
		Helpers.setPermissions(READ_AHEAD_FILE);
		Helpers.setPermissions(CPUQUIET_DIR);
		Helpers.setPermissions(CPUQUIET_GOVERNORS);

		frequencies = Helpers.getFrequencies();
		names = Helpers.getFrequenciesNames();
		governors = Helpers.getGovernors();
		String gpu = Helpers.getFileContent(new File(GPU_FREQUENCIES_FILE));
		gpuFrequencies = gpu.split(" ");
		String[] gpuNames = Helpers.getFreqToMhz(GPU_FREQUENCIES_FILE);
		schedulers = Helpers.getAvailableSchedulers();
		db = new DatabaseHandler(mContext);
		VddDb = new VddDatabaseHandler(mContext);
		items = db.getAllItems();
		vddItems = VddDb.getAllItems();

		if(new File(CPUQUIET_DIR).exists()) {
			String cpuquiet = Helpers.getFileContent(new File(CPUQUIET_GOVERNORS));
			cpuquiet_govs = cpuquiet.trim().replaceAll("\n", "").split(" ");
		}

		if(items.size() != 0) {
			for(DataItem item : items) {
				String fPath = item.getName().replaceAll("'", "");
				String fName = item.getFileName();
				Log.d("PATH", fPath);
				String value = item.getValue();
				String category = item.getCategory();
				if(category.equals(cpuCat)) {
					String color = getColor(2);
					if(fName.contains("CPU Max Frequency")) {
						createListPreference(mCpu,fPath,fName,value,frequencies, names,color,category,false);
					}
					else if(fName.contains("CPU Min Frequency")) {
						createListPreference(mCpu,fPath,fName,value,frequencies, names,color,category,false);
					}
					else if(fName.contains("Governor")) {
						createListPreference(mCpu,fPath,fName,value,governors, governors,color,category,false);
					} else if(fName.contains("Cpuquiet")) {
						createListPreference(mCpu,fPath,fName,value,cpuquiet_govs,cpuquiet_govs,color,category,false);
					}else {
						createPreference(mCpu,fPath, fName, value, color, category, false);
					}
				}
				else if (category.equals(gpuCat)) {
					String color = getColor(3);
					if(fName.contains("GPU Max Frequency")) {
						createListPreference(mGpu,fPath, fName, value, gpuFrequencies, gpuNames, color, category, false);
					}else {
						createPreference(mGpu,fPath, fName, value, color, category, false);
					}
				}
				else if(category.equals(uvCat)) {
					String color = getColor(4);
					createPreference(mUv,fPath, fName, value, color, category, false);
				}
				else if(category.equals(kernelCat)) {
					String color = getColor(5);
					if(fName.contains("I/O Scheduler")) {
						createListPreference(mKernel,fPath, fName, value, schedulers, schedulers, color, category, false);
					}else if(fName.contains("Read Ahead size")) {
						createListPreference(mKernel,fPath, fName, value, readAheadKb,readAheadKb, color, category, false);
					}else if(fName.contains("TCP Congestion control")) {
						String[] availTCP = Helpers.readCommandStrdOut(TCP_OPTIONS, false).replaceAll("net.ipv4.tcp_available_congestion_control = ", "").replaceAll("\n", "").split(" ");
						createListPreference(mKernel, fPath, fName, value, availTCP, availTCP, color, category, false);
					}
					else {
						createPreference(mKernel,fPath, fName, value, color, category, false);
					}
				}
				else if(category.equals(LmkCat)) {
					String color = getColor(6);
					createPreference(mLmk,fPath, fName, value, color, category, false);
				}
				else if(category.equals(GovCat)) {
					String color = getColor(12);
					createPreference(mGov,fPath, fName, value, color, category, false);
				}
				else if(category.equals(SchedCat)) {
					String color = getColor(12);
					createPreference(mSched,fPath, fName, value, color, category, false);
				}
				else if(category.equals(QuietCat)) {
					String color = getColor(12);
					createPreference(mQuiet,fPath, fName, value, color, category, false);
				}
				else if(category.equals(vmCat)) {
					String color = getColor(7);
					createPreference(mVm,fPath, fName, value, color, category, false);
				}

			}
		} 

		if(vddItems.size() != 0) {
			String color = getResources().getStringArray(R.array.menu_colors)[2];
			createPreference(mUv,"", 
					getResources().getString(R.string.vdd_pref),
					getResources().getString(R.string.vdd_desc),
					color,
					"uv",
					true);
		}

		checkEmpty();
		if(mRoot.getPreferenceCount() == 0) {
			addEmptyView();
		}
		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
		setRetainInstance(true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenu.ContextMenuInfo menuInfo) {
		getActivity().getMenuInflater().inflate(R.menu.main, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		return super.onContextItemSelected(item);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);

		listView = (ListView) v.findViewById(android.R.id.list);
		listView.setFastScrollEnabled(true);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
		registerForContextMenu(listView);
		listView.setMultiChoiceModeListener(new ListViewMultiChoiceModeListener(
				mContext,getActivity(),
				listView,mRoot,
				mCpu,
				mGpu,
				mUv,
				mKernel,
				mLmk,
				mGov,
				mSched,
				mQuiet,
				mVm,
				db,
				VddDb,
				true));


		return v;
	}


	private void createPreference(PreferenceCategory mCategory,  
			String fPath, String fName, String value, String color, 
			final String category, boolean excludeEdit) {

		final CustomPreference pref = new CustomPreference(mContext, false, category);
		pref.setTitle(fName);
		pref.setTitleColor(color);
		pref.setSummary(value);
		pref.setKey(fPath);
		pref.hideBoot(true);
		Log.d("CONTENT", value);
		mCategory.addPreference(pref);
		if(!excludeEdit) {
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
					//et.setRawInputType(InputType.TYPE_CLASS_NUMBER);
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
							CMDProcessor.runSuCommand("echo \""+value+"\" > "+p.getKey());
							updateDb(p, value, true, category);
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

	@Override
	public void onDetach() {
		super.onDetach();
		mRoot.removeAll();
	}


	private void createListPreference(PreferenceCategory mCategory,  
			String fPath, String fName, String value,String[] entries, String[] names, String color, 
			final String category, boolean excludeEdit) {

		final CustomListPreference pref = new CustomListPreference(mContext, category);
		pref.setTitle(fName);
		pref.setTitleColor(color);
		pref.setSummary(value);
		pref.setEntries(names);
		pref.setEntryValues(entries);
		pref.hideBoot(true);
		pref.setKey(fPath);
		Log.d("CONTENT", value);
		mCategory.addPreference(pref);
		if(!excludeEdit) {
			pref.setOnPreferenceChangeListener(new OnPreferenceChangeListener(){

				@Override
				public boolean onPreferenceChange(final Preference p, Object newValue) {
					// TODO Auto-generated method stub
					p.setSummary((String)newValue);
					pref.setValue(p.getSummary().toString());
					CMDProcessor.runSuCommand("echo \""+(String)newValue+"\" > "+p.getKey());
					updateDb(p, (String)newValue, true, category);
					return true;
				}

			});
		}
	}



	private void updateDb(final Preference p, final String value,final boolean isChecked, final String category) {

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
					if(p.getTitle().toString().contains("TCP")) {
						db.addItem(new DataItem("'"+"sysctl -w net.ipv4.tcp_congestion_control="+value+"'", value, p.getTitle().toString(), category));
					} else {
						db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
					}
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

	public void addEmptyView() {
		mRoot.removeAll();
		CustomPreference pref = new CustomPreference(mContext, true, "");
		pref.setTitle("EMPTY");
		pref.setSummary("No Values set at boot");
		String color = getResources().getStringArray(R.array.menu_colors)[8];
		pref.setTitleColor(color);
		pref.setSummaryColor(color);
		pref.hideBoot(true);
		mRoot.addPreference(pref);
	}




	private void checkEmpty() {

		if(mCpu.getPreferenceCount() == 0) {
			mRoot.removePreference(mCpu);
		}
		if(mGpu.getPreferenceCount() == 0) {
			mRoot.removePreference(mGpu);
		}
		if(mUv.getPreferenceCount() == 0) {
			mRoot.removePreference(mUv);
		}
		if(mKernel.getPreferenceCount() == 0) {
			mRoot.removePreference(mKernel);
		}
		if(mLmk.getPreferenceCount() == 0) {
			mRoot.removePreference(mLmk);
		}
		if(mGov.getPreferenceCount() == 0) {
			mRoot.removePreference(mGov);
		}
		if(mSched.getPreferenceCount() == 0) {
			mRoot.removePreference(mSched);
		}
		if(mQuiet.getPreferenceCount() == 0) {
			mRoot.removePreference(mQuiet);
		}
		if(mVm.getPreferenceCount()==0) {
			mRoot.removePreference(mVm);
		}
	}

	private String getColor(int pos) {
		String color = "";
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FF0099cc"));
			color = "#"+Integer.toHexString(col);
		}else if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int col = Color.parseColor("#ff0099cc");
			switch(pos) {
			case 0:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_STAT, Color.parseColor("#FFFFFF"));
				break;
			case 1:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_INFO, Color.parseColor("#FFFFFF"));
				break;
			case 2:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_CPU, Color.parseColor("#FFFFFF"));
				break;
			case 3:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GPU, Color.parseColor("#FFFFFF"));
				break;
			case 4:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_UV, Color.parseColor("#FFFFFF"));
				break;
			case 5:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_KERNEL, Color.parseColor("#FFFFFF"));
				break;
			case 6:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_LMK, Color.parseColor("#FFFFFF"));
				break;
			case 7:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_VM, Color.parseColor("#FFFFFF"));
				break;
			case 8:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_REVIEW, Color.parseColor("#FFFFFF"));
				break;
			case 9:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_FILE, Color.parseColor("#FFFFFF"));
				break;
			case 10:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_BAK, Color.parseColor("#FFFFFF"));
				break;
			case 11:
				col = MainActivity.mPrefs.getInt(SettingsFragment.KEY_RECOVERY, Color.parseColor("#FFFFFF"));
				break;
			}
			color = "#"+Integer.toHexString(col);
		}
		else {
			color = getResources().getStringArray(R.array.menu_colors)[pos];
		}
		return color;
	}
}
