package com.dsht.kerneltweaker.fragments;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.dsht.kerneltweaker.BackupBaseAdapter;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.kerneltweaker.SwipeDismissListViewTouchListener;
import com.dsht.kerneltweaker.SwipeDismissListViewTouchListener.DismissCallbacks;
import com.dsht.kernetweaker.cmdprocessor.CMDProcessor;
import com.dsht.settings.SettingsFragment;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class BackupFragment extends Fragment implements OnClickListener, OnItemClickListener {

	private Context mContext;
	private LinearLayout mBackupBoot;
	private LinearLayout mBackupRecovery;
	private ListView mList;
	private List<File> listFiles;
	private BackupBaseAdapter mAdapter;
	private File backupDir;
	private SwipeDismissListViewTouchListener touchListener;
	private TextView mBoot;
	private TextView mRecovery;
	private ImageView mBootImage;
	private ImageView mRecoveryImage;
	private View mSepar1;
	private View mSepar2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = getActivity();
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.backup_list, container,false);
		mList = (ListView) v.findViewById(R.id.navbarlist);
		mBackupBoot = (LinearLayout) v.findViewById(R.id.backup_boot);
		mBackupRecovery = (LinearLayout) v.findViewById(R.id.backup_recovery);
		LinearLayout mEmpty = (LinearLayout) v.findViewById(R.id.empty);
		mList.setEmptyView(mEmpty);

		mBoot = (TextView) v.findViewById(R.id.titleboot);
		mRecovery = (TextView) v.findViewById(R.id.titlerecovery);
		mBootImage = (ImageView) v.findViewById(R.id.image);
		mRecoveryImage = (ImageView) v.findViewById(R.id.image2);
		mSepar1 = (View) v.findViewById(R.id.view1);
		mSepar2 = (View) v.findViewById(R.id.View2);
		if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_GLOBAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_GLOBAL_COLOR, Color.parseColor("#FFFFFF"));
			mBoot.setTextColor(color);
			mRecovery.setTextColor(color);
			mBootImage.setColorFilter(color);
			mRecoveryImage.setColorFilter(color);
			mSepar1.setBackgroundColor(color);
			mSepar2.setBackgroundColor(color);
		}else if(MainActivity.mPrefs.getBoolean(SettingsFragment.KEY_ENABLE_PERSONAL, false)) {
			int color = MainActivity.mPrefs.getInt(SettingsFragment.KEY_BAK, 0);
			mBoot.setTextColor(color);
			mRecovery.setTextColor(color);
			mBootImage.setColorFilter(color);
			mRecoveryImage.setColorFilter(color);
			mSepar1.setBackgroundColor(color);
			mSepar2.setBackgroundColor(color);
		}


		backupDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/K-BACKUPS");
		if(!backupDir.exists()) {
			backupDir.mkdirs();
		}
		listFiles = list(backupDir.listFiles());	
		mAdapter = new BackupBaseAdapter(mContext, listFiles);
		mList.setAdapter(mAdapter);
		mBackupBoot.setOnClickListener(this);
		mBackupRecovery.setOnClickListener(this);

		touchListener =
				new SwipeDismissListViewTouchListener(
						mList,
						new DismissCallbacks() {
							public void onDismiss(ListView listView, int[] reverseSortedPositions) {
								for (int position : reverseSortedPositions) {
									File f = listFiles.get(position);
									f.delete();
									mAdapter.remove(mAdapter.getItem(position));

								}
								mAdapter.notifyDataSetChanged();
							}

							@Override
							public boolean canDismiss(int position) {
								// TODO Auto-generated method stub
								return true;
							}
						});
		mList.setOnTouchListener(touchListener);
		mList.setOnScrollListener(touchListener.makeScrollListener());
		mList.setOnItemClickListener(this);
		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}

		return v;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.backup_boot:
			doBackup(true);
			break;
		case R.id.backup_recovery:
			doBackup(false);
			break;
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		showDialog(arg2);
	}


	private void doBackup(final boolean boot) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LinearLayout ll = new LinearLayout(mContext);
		ll.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
		final EditText et = new EditText(mContext);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(40, 40, 40, 40);
		params.gravity = Gravity.CENTER;
		et.setLayoutParams(params);
		et.setGravity(Gravity.CENTER_HORIZONTAL);
		et.setHint("backup name...");;
		ll.addView(et);
		builder.setView(ll);
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String value = et.getText().toString();
				if(!value.contains(".img")){
					value +=".img";
				}
				backup(boot, value);
				/*if(boot) {
					CMDProcessor.runSuCommand("dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of="+backupDir.getAbsolutePath()+"/"+value);
				}else {
					CMDProcessor.runSuCommand("dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of="+backupDir.getAbsolutePath()+"/"+value);
				}
				listFiles.clear();
				listFiles = list(backupDir.listFiles());
				mAdapter.notifyDataSetChanged(); */
			}
		} );
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
		Window window = dialog.getWindow();
		window.setLayout(800, LayoutParams.WRAP_CONTENT);
	}

	public List<File> list(File[] files) {
		List<File> newList = new ArrayList<File>();
		for(File file : files) {
			newList.add(file);
		}
		return newList;
	}

	private void showDialog(final int position) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setTitle(listFiles.get(position).getName());
		builder.setItems(R.array.install_image_dialog, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				switch(which) {
				case 0:
					installBoot(listFiles.get(position).getAbsolutePath());
					break;
				case 1:
					installRecovery(listFiles.get(position).getAbsolutePath());
					break;
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
		dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_animation;
		Window window = dialog.getWindow();
		window.setLayout(800, LayoutParams.WRAP_CONTENT);
	}

	public void installBoot(String bootPath) {
		CommandCapture cmd = new CommandCapture(0,"dd if="+bootPath+" of=/dev/block/platform/msm_sdcc.1/by-name/boot", "reboot");
		try {
			RootTools.getShell(true).add(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RootDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void installRecovery(String recoveryPath) {
		CommandCapture cmd = new CommandCapture(0,"dd if="+recoveryPath+" of=/dev/block/platform/msm_sdcc.1/by-name/recovery", "reboot");
		try {
			RootTools.getShell(true).add(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RootDeniedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void backup(final boolean boot, final String value) {
		class LongOperation extends AsyncTask<String, Void, String> {

			ProgressDialog pd;

			@Override
			protected void onPreExecute() {

				pd = new ProgressDialog(mContext);
				pd.setIndeterminate(true);
				pd.setMessage("Backing up...Please wait");
				pd.setCancelable(false);
				pd.show();

			}

			@Override
			protected String doInBackground(String... params) {
				if(boot) {
					CMDProcessor.runSuCommand("dd if=/dev/block/platform/msm_sdcc.1/by-name/boot of="+backupDir.getAbsolutePath()+"/"+value);
				}else {
					CMDProcessor.runSuCommand("dd if=/dev/block/platform/msm_sdcc.1/by-name/recovery of="+backupDir.getAbsolutePath()+"/"+value);
				}
				return "executed";
			}
			@Override
			protected void onPostExecute(String result) {
				listFiles = list(backupDir.listFiles());
				mAdapter = new BackupBaseAdapter(mContext, listFiles);
				mList.setAdapter(mAdapter);
				pd.dismiss();
			}
		}
		new LongOperation().execute();
	}

}
