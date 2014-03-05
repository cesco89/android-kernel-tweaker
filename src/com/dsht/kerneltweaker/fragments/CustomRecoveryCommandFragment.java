package com.dsht.kerneltweaker.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.RecoveryBaseAdapter;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CustomRecoveryCommandFragment extends Fragment implements OnClickListener, OnCheckedChangeListener {

	private Context mContext;
	private DragSortListView mList;
	private ArrayList<String> list;
	//private ArrayAdapter<String> adapter;
	private String[] dialogEntries = {"Wipe Cache","Wipe Dalvik","Wipe Data/Factory reset","Install package", "Nandroid Backup"};
	private String[] dialogValues = {"wipe cache", "wipe dalvik", "wipe data"};
	private ArrayList<String> names;
	private ArrayList<String> values;
	private static final String COMMAND_FILE = "/cache/recovery/openrecoveryscript";
	private RecoveryBaseAdapter mAdapter;
	private static final int READ_REQUEST_CODE = 42;
	private Button mReboot;
	private Button mCancel;
	private ArrayList<String> backupString;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		mContext = getActivity();

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
		setRetainInstance(true);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.dragsort_list, container,false);
		mList = (DragSortListView) v.findViewById(R.id.dslist);
		mReboot = (Button) v.findViewById(R.id.reboot);
		mCancel = (Button) v.findViewById(R.id.cancel);
		mReboot.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mList.setEmptyView(v.findViewById(R.id.empty));
		list = new ArrayList<String>();
		names = new ArrayList<String>();
		values = new ArrayList<String>();
		backupString = new ArrayList<String>();

		for(int i=0; i<10; i++) {
			list.add("TEST "+i);
		}

		mAdapter = new RecoveryBaseAdapter(mContext, names, values);

		mList.setAdapter(mAdapter);
		mList.setDropListener(onDrop);
		mList.setRemoveListener(onRemove);

		DragSortController controller = new DragSortController(mList);
		controller.setDragHandleId(R.id.image);
		//controller.setClickRemoveId(R.id.);
		controller.setRemoveEnabled(true);
		controller.setSortEnabled(true);
		controller.setDragInitMode(1);
		controller.setFlingHandleId(R.id.item_container);
		//controller.setRemoveMode(removeMode);

		mList.setFloatViewManager(controller);
		mList.setOnTouchListener(controller);
		mList.setDragEnabled(true);
		checkEmptyList();
		return v;
	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_recovery, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_plus:
			showDialog();
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent resultData) {
		if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
			Uri uri = null;
			if (resultData != null) {
				uri = resultData.getData();
				String path = uri.getPath();
				if(path.endsWith(".zip")) {
					names.add(dialogEntries[3]);
					values.add("install "+path);
					mAdapter.notifyDataSetChanged();
					buildFile();
					checkEmptyList();
				} else {
					notZip();
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.cancel:
			onCancelPressed();
			break;
		case R.id.reboot:
			onRebootPressed();
			break;
		}
	}

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
	{
		@Override
		public void drop(int from, int to)
		{
			if (from != to)
			{
				String nameItem = mAdapter.getNameItem(from);
				String valueItem = mAdapter.getValueItem(from);
				mAdapter.remove(nameItem, valueItem);
				mAdapter.insert(nameItem, valueItem, to, to);
				buildFile();
			}
		}
	};

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener()
	{
		@Override
		public void remove(int which)
		{
			String nameItem = mAdapter.getNameItem(which);
			String valueItem = mAdapter.getValueItem(which);
			mAdapter.remove(nameItem, valueItem);
			buildFile();
			if(names.size() == 0) {
				removeFile();
				checkEmptyList();
			}
		}
	};


	private void showDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle("Add action");
		builder.setItems(dialogEntries, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which) {
				case 0:
						addCommand(0);
					break;
				case 1:
						addCommand(1);
					break;
				case 2:
						addCommand(2);
					break;
				case 3:
					performFileSearch();
					break;
				case 4:
					backupDialog();
					break;
				}
			}
		});
		builder.create().show();
	}


	private void addCommand(int position) {

		names.add(dialogEntries[position]);
		values.add(dialogValues[position]);
		mAdapter.notifyDataSetChanged();
		buildFile();
		checkEmptyList();
	}

	private boolean alreadyAdded(int position) {
		if(names.contains(dialogEntries[position])) {
			return true;
		}
		return false;
	}

	private void buildFile() {
		removeFile();
		for(String str : values) {
			CMDProcessor.runSuCommand("echo \"" + str + "\" >> "+ COMMAND_FILE);
		}
	}

	private void removeFile() {
		CMDProcessor.runSuCommand("rm -f "+COMMAND_FILE);
	}

	public void performFileSearch() {

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		startActivityForResult(intent, READ_REQUEST_CODE);
	}

	private void checkEmptyList() {
		if(names.size() == 0) {
			mReboot.setEnabled(false);
			mCancel.setEnabled(false);
		} else {
			mReboot.setEnabled(true);
			mCancel.setEnabled(true);
		}
	}

	private void onCancelPressed() {
		values.clear();
		names.clear();
		removeFile();
		mAdapter.notifyDataSetChanged();
		checkEmptyList();
	}

	private void onRebootPressed() {
		CMDProcessor.runSuCommand("reboot recovery");
	}


	private void notZip() {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage("Selected File is not a Zip!!");
		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.create().show();
	}

	private String buildBackupString() {
		String composed = "backup ";
		for(String str : backupString) {
			composed +=str;
		}
		return composed;
	}

	private void backupDialog() {
		backupString.clear();
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.backup_dialog_layout, null, false);
		final CheckBox mSystem = (CheckBox) v.findViewById(R.id.cb_system);
		final CheckBox mData = (CheckBox) v.findViewById(R.id.cb_data);
		final CheckBox mCache = (CheckBox) v.findViewById(R.id.cb_cache);
		final CheckBox mBoot = (CheckBox) v.findViewById(R.id.cb_boot);
		final CheckBox mRecovery = (CheckBox) v.findViewById(R.id.cb_recovery);
		final CheckBox mMd5 = (CheckBox) v.findViewById(R.id.cb_md5);
		final CheckBox mSecure = (CheckBox) v.findViewById(R.id.cb_secure);
		final CheckBox mCompression = (CheckBox) v.findViewById(R.id.cb_compression);
		final EditText mName = (EditText) v.findViewById(R.id.et_name);
		mName.setHint(Build.DISPLAY);
		mSystem.setOnCheckedChangeListener(this);
		mData.setOnCheckedChangeListener(this);
		mCache.setOnCheckedChangeListener(this);
		mBoot.setOnCheckedChangeListener(this);
		mRecovery.setOnCheckedChangeListener(this);
		mMd5.setOnCheckedChangeListener(this);
		mSecure.setOnCheckedChangeListener(this);
		mCompression.setOnCheckedChangeListener(this);
		builder.setView(v);
		builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				// TODO Auto-generated method stub
				String name = "";
				if(mName.getText().toString().equals("")) {
					name = Build.DISPLAY;
				}else {
					name = mName.getText().toString();
				}
				String command = buildBackupString() + " "+name;
				names.add(dialogEntries[4]);
				values.add(command);
				mAdapter.notifyDataSetChanged();
				buildFile();
				checkEmptyList();

			}
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		});
		builder.create().show();
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
		// TODO Auto-generated method stub
		switch(arg0.getId()) {
		case R.id.cb_system:
			if(isChecked) {
				backupString.add("S");
			}else {
				if(backupString.contains("S")) {
					backupString.remove("S");
				}
			}
			break;
		case R.id.cb_data:
			if(isChecked) {
				backupString.add("D");
			}else {
				if(backupString.contains("D")) {
					backupString.remove("D");
				}
			}
			break;
		case R.id.cb_cache:
			if(isChecked) {
				backupString.add("C");
			}else {
				if(backupString.contains("C")) {
					backupString.remove("C");
				}
			}
			break;
		case R.id.cb_boot:
			if(isChecked) {
				backupString.add("B");
			}else {
				if(backupString.contains("B")) {
					backupString.remove("B");
				}
			}
			break;
		case R.id.cb_recovery:
			if(isChecked) {
				backupString.add("R");
			}else {
				if(backupString.contains("R")) {
					backupString.remove("R");
				}
			}
			break;
		case R.id.cb_md5:
			if(isChecked) {
				backupString.add("M");
			}else {
				if(backupString.contains("M")) {
					backupString.remove("M");
				}
			}
			break;
		case R.id.cb_secure:
			if(isChecked) {
				backupString.add("A");
			}else {
				if(backupString.contains("A")) {
					backupString.remove("A");
				}
			}
			break;
		case R.id.cb_compression:
			if(isChecked) {
				backupString.add("O");
			}else {
				if(backupString.contains("O")) {
					backupString.remove("O");
				}
			}
			break;
		}

	}
}
