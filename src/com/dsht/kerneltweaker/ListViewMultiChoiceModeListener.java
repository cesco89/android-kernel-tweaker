package com.dsht.kerneltweaker;

import java.util.List;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)

public class ListViewMultiChoiceModeListener implements
AbsListView.MultiChoiceModeListener {
	Activity host;
	ActionMode activeMode;
	ListView lv;
	Context mContext;
	PreferenceScreen mRoot;
	PreferenceCategory mCpu;
	PreferenceCategory mGpu;
	PreferenceCategory mUv;
	PreferenceCategory mKernel;
	PreferenceCategory mLmk;
	PreferenceCategory mGov;
	PreferenceCategory mSched;
	PreferenceCategory mQuiet;
	PreferenceCategory mVm;
	DatabaseHandler db;
	VddDatabaseHandler VddDb;
	boolean delete;
	MenuItem mDelete;
	MenuItem mAdd;

	private static final String cpuCat = "cpu";
	private static final String gpuCat = "gpu";
	private static final String uvCat ="uv";
	private static final String kernelCat = "kernel";
	private static final String LmkCat = "lmk";
	private static final String GovCat = "governor";
	private static final String SchedCat ="scheduler";
	private static final String QuietCat ="cpuquiet";
	private static final String vmCat ="vm";

	public ListViewMultiChoiceModeListener(Context mContext,Activity host, ListView lv, 
			PreferenceScreen mRoot,
			PreferenceCategory mCpu,
			PreferenceCategory mGpu,
			PreferenceCategory mUv,
			PreferenceCategory mKernel,
			PreferenceCategory mLmk,
			PreferenceCategory mGov,
			PreferenceCategory mSched,
			PreferenceCategory mQuiet,
			PreferenceCategory mVm,
			DatabaseHandler db,
			VddDatabaseHandler vddDb,
			boolean delete) {
		this.host=host;
		this.lv=lv;
		this.mRoot = mRoot;
		this.mCpu = mCpu;
		this.mGpu = mGpu;
		this.mUv = mUv;
		this.mKernel = mKernel;
		this.mLmk = mLmk;
		this.db = db;
		this.VddDb = vddDb;
		this.delete = delete;
		this.mGov = mGov;
		this.mSched = mSched;
		this.mQuiet = mQuiet;
		this.mVm = mVm;
		this.mContext = mContext;

	}

	public ListViewMultiChoiceModeListener(Context mContext,
			Activity host, ListView lv, 
			PreferenceScreen mRoot, boolean delete,
			DatabaseHandler db, VddDatabaseHandler VddDb) {
		this.mContext = mContext;
		this.host = host;
		this.lv = lv;
		this.mRoot = mRoot;
		this.delete = delete;
		this.db = db;
		this.VddDb = VddDb;
	}

	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		MenuInflater inflater=host.getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		mDelete = (MenuItem) menu.findItem(R.id.action_delete);
		mAdd = (MenuItem) menu.findItem(R.id.action_add);

		if(delete) {
			mode.setTitle("Delete items");
			mAdd.setVisible(false);
		}else {
			mDelete.setVisible(false);
			mode.setTitle("Add on boot");
		}
		mode.setSubtitle("1 Item Selected");
		activeMode=mode;

		return(true);
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return(false);
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

		updateSubtitle(activeMode);

		switch(item.getItemId()) {
		case R.id.action_delete:
			SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
			SharedPreferences.Editor editor = mPrefs.edit();
			int len = lv.getCount();
			SparseBooleanArray checked = lv.getCheckedItemPositions();
			for (int i = 0; i < len; i++)
				if (checked.get(i)) {
					Object o = mRoot.getRootAdapter().getItem(i);
					if(o instanceof PreferenceCategory) {
						//do nothing
					}else if( o instanceof CustomPreference) {

						CustomPreference pref = (CustomPreference) o;
						editor.remove(pref.getTitle().toString());

						if(pref.getCategory().equals(cpuCat)) {
							mCpu.removePreference(pref);
						}
						if(pref.getCategory().equals(gpuCat)) {
							mGpu.removePreference(pref);
						}
						if(pref.getCategory().equals(uvCat)) {
							mUv.removePreference(pref);
						}
						if(pref.getCategory().equals(kernelCat)) {
							mKernel.removePreference(pref);
						}
						if(pref.getCategory().equals(LmkCat)) {
							mLmk.removePreference(pref);
						}
						if(pref.getCategory().equals(GovCat)) {
							mGov.removePreference(pref);
						}
						if(pref.getCategory().equals(SchedCat)) {
							mSched.removePreference(pref);
						}
						if(pref.getCategory().equals(QuietCat)) {
							mQuiet.removePreference(pref);
						}
						if(pref.getCategory().equals(vmCat)) {
							mVm.removePreference(pref);
						}
						if(pref.getTitle().toString().contains("VDD")) {
							VddDb.deleteAllItems();
						}else {
							String name = pref.getKey();
							db.deleteItemByName("'"+name+"'");
						}
						checkEmpty();
					}
					else if( o instanceof CustomListPreference) {

						CustomListPreference pref = (CustomListPreference) o;
						editor.remove(pref.getTitle().toString());
						if(pref.getTitle().toString().contains("VDD")) {
							VddDb.deleteAllItems();
						}else {
							String name = pref.getKey();
							db.deleteItemByName("'"+name+"'");
						}
						if(pref.getCategory().equals(cpuCat)) {
							mCpu.removePreference(pref);
						}
						if(pref.getCategory().equals(gpuCat)) {
							mGpu.removePreference(pref);
						}
						if(pref.getCategory().equals(uvCat)) {
							mUv.removePreference(pref);
						}
						if(pref.getCategory().equals(kernelCat)) {
							mKernel.removePreference(pref);
						}
						if(pref.getCategory().equals(LmkCat)) {
							mLmk.removePreference(pref);
						}
						if(pref.getCategory().equals(GovCat)) {
							mGov.removePreference(pref);
						}
						if(pref.getCategory().equals(SchedCat)) {
							mSched.removePreference(pref);
						}
						if(pref.getCategory().equals(QuietCat)) {
							mQuiet.removePreference(pref);
						}
						if(pref.getCategory().equals(vmCat)) {
							mVm.removePreference(pref);
						}
						if(pref.getTitle().toString().contains("VDD")) {
							VddDb.deleteAllItems();
						}

						checkEmpty();
					}
					editor.commit();
				}
			if(mRoot.getPreferenceCount() == 0) {
				addEmptyView();
			}
			break;
		case R.id.action_add:
			int length = lv.getCount();
			SparseBooleanArray check = lv.getCheckedItemPositions();
			List<DataItem> items = db.getAllItems();
			for (int i = 0; i < length; i++) {
				if (check.get(i)) {
					Object o = mRoot.getRootAdapter().getItem(i);
					if( o instanceof CustomPreference) {
						CustomPreference p = (CustomPreference)o;
						if(p.getTitle().toString().contains("Information")) {

						}else if(p.getTitle().toString().contains("EMPTY")) {

						}else if(!p.getTitle().toString().contains("Tuning")) {
							for(DataItem it : items) {
								if(it.getName().equals("'"+p.getKey()+"'")) {
									db.deleteItemByName("'"+p.getKey()+"'");
								}
							}
							db.addItem(new DataItem("'"+p.getKey()+"'", p.getSummary().toString(), p.getTitle().toString(), p.getCategory()));
						}
					}
					if( o instanceof CustomListPreference) {
						CustomListPreference p = (CustomListPreference)o;
						for(DataItem it : items) {
							if(it.getName().equals("'"+p.getKey()+"'")) {
								db.deleteItemByName("'"+p.getKey()+"'");
							}
						}
						db.addItem(new DataItem("'"+p.getKey()+"'", p.getSummary().toString(), p.getTitle().toString(), p.getCategory()));
					}
					if(o instanceof CustomCheckBoxPreference) {
						CustomCheckBoxPreference p = (CustomCheckBoxPreference)o;
						for(DataItem it : items) {
							if(it.getName().equals("'"+p.getKey()+"'")) {
								db.deleteItemByName("'"+p.getKey()+"'");
							}
						}
						db.addItem(new DataItem("'"+p.getKey()+"'", p.getValue(), p.getTitle().toString(), p.getCategory()));
					}
				}
			}
			break;
		}
		mode.finish();
		return true;
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		activeMode=null;
	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {
		updateSubtitle(mode);

	}

	private void updateSubtitle(ActionMode mode) {
		mode.setSubtitle( lv.getCheckedItemCount() + " Items selected");
	}

	public void addEmptyView() {
		mRoot.removeAll();
		CustomPreference pref = new CustomPreference(host, true, "");
		pref.setTitle(mContext.getResources().getString(R.string.emp_title));
		pref.setSummary(mContext.getResources().getString(R.string.emp_desc));
		pref.hideBoot(true);
		String color = host.getResources().getStringArray(R.array.menu_colors)[6];
		pref.setTitleColor(color);
		pref.setSummaryColor(color);
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
		if(mQuiet.getPreferenceCount() == 0) {
			mRoot.removePreference(mQuiet);
		}
		if(mSched.getPreferenceCount() == 0) {
			mRoot.removePreference(mSched);
		}
		if(mVm.getPreferenceCount() == 0) {
			mRoot.removePreference(mVm);
		}
	}
}
