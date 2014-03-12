package com.dsht.kerneltweaker;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import com.dsht.glossary.CpuGlossaryFragment;
import com.dsht.glossary.GpuGlossaryFragment;
import com.dsht.glossary.KernelGlossaryFragment;
import com.dsht.glossary.LmkGlossaryFragment;
import com.dsht.glossary.UvGlossaryFragment;
import com.dsht.glossary.VmGlossaryFragment;
import com.dsht.kerneltweaker.database.DatabaseHandler;
import com.dsht.kerneltweaker.database.VddDatabaseHandler;
import com.dsht.kerneltweaker.fragments.BackupFragment;
import com.dsht.kerneltweaker.fragments.CpuPreferenceFragment;
import com.dsht.kerneltweaker.fragments.CustomRecoveryCommandFragment;
import com.dsht.kerneltweaker.fragments.FileManagerFragment;
import com.dsht.kerneltweaker.fragments.GpuPreferenceFragment;
import com.dsht.kerneltweaker.fragments.InitD;
import com.dsht.kerneltweaker.fragments.KernelPreferenceFragment;
import com.dsht.kerneltweaker.fragments.LowMemoryKillerFragment;
import com.dsht.kerneltweaker.fragments.PropModder;
import com.dsht.kerneltweaker.fragments.ReviewBootPreferenceFragment;
import com.dsht.kerneltweaker.fragments.UvPreferenceFragment;
import com.dsht.kerneltweaker.fragments.WallpaperEffectsFragment;
import com.dsht.open.CPUInfo;
import com.dsht.open.TimeInState;
import com.dsht.open.VM;
import com.dsht.settings.SettingsFragment;
import com.dsht.settings.infos;
import com.google.analytics.tracking.android.EasyTracker;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.stericson.RootTools.RootTools;
import com.stericson.RootTools.exceptions.RootDeniedException;
import com.stericson.RootTools.execution.CommandCapture;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ListView;

public class MainActivity extends FragmentActivity implements OnItemClickListener {

	private static FrameLayout mContainer;
	public static SlidingMenu menu;
	private String[] colors;
	public static DatabaseHandler db;
	public static VddDatabaseHandler vddDb;
	public static Context mContext;
	public static SharedPreferences mPrefs;
	public static ListView menulist;
	private static FrameLayout mGlossaryContainer;
	public static CustomArrayAdapter mAdapter;
	
	public static final int[] icons = {
		0,
		R.drawable.meter,
		R.drawable.bar_chart,
		0,
		R.drawable.flash_on,
		R.drawable.lcd,
		R.drawable.plus_minus,
		0,
		R.drawable.beaker,
		R.drawable.life_guard,
		R.drawable.settings_two,
		0,
		R.drawable.heart,
		0,
		R.drawable.doc_zip,
		R.drawable.backup,
		R.drawable.radiation,
		0,
		R.drawable.doc,
		R.drawable.magic_wand,
		R.drawable.eye,
		0,
		R.drawable.settings_one,
		R.drawable.info
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_container);
		RootTools.debugMode = false;
		
		boolean access = RootTools.isAccessGiven();
		boolean busybox = RootTools.isBusyboxAvailable();
		if(!access) {
			showRootWarning();
		}
		if(!busybox) {
			showBusyBoxWarning();
		}
		
		mContainer = (FrameLayout) findViewById(R.id.activity_container);
		mContext = this;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		View v = this.getLayoutInflater().inflate(R.layout.menu_list, null, false);
		menulist = (ListView) v.findViewById(R.id.navbarlist);
		db = new DatabaseHandler(this);
		vddDb = new VddDatabaseHandler(this);

		menu = new SlidingMenu(this);
		menu.setMode(SlidingMenu.LEFT_RIGHT);
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setShadowWidthRes(R.dimen.shadow_width);
		menu.setShadowDrawable(R.drawable.shadow);
		//menu.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		menu.setBehindWidthRes(R.dimen.slidingmenu_offset);
		menu.setFadeDegree(0.35f);
		menu.attachToActivity(this, SlidingMenu.SLIDING_CONTENT);
		menu.setMenu(v);
		View vv = this.getLayoutInflater().inflate(R.layout.menu_glossary, null, false);
		mGlossaryContainer = (FrameLayout) vv.findViewById(R.id.menu_frame);
		menu.setSecondaryMenu(vv);
		menu.setSecondaryShadowDrawable(R.drawable.shadow_right);

		mAdapter = new CustomArrayAdapter(
				this,
				R.layout.menu_main_list_item,
				getResources().getStringArray(R.array.menu_entries), 
				getResources().getStringArray(R.array.menu_descs),
				getResources().getStringArray(R.array.menu_colors),
				icons);
		menulist.setAdapter(mAdapter);
		menulist.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		menulist.setOnItemClickListener(this);

		colors = getResources().getStringArray(R.array.menu_colors);

		FragmentTransaction ft = getFragmentManager().beginTransaction();
		Fragment prefs = new TimeInState();
		CpuGlossaryFragment glo = new CpuGlossaryFragment();
		ft.replace(R.id.activity_container,prefs);
		ft.replace(R.id.menu_frame, glo);
		ft.commit();

		
		setAppTheme();
		mountPartitions();
		copyHelpers();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main_container, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case android.R.id.home: 
			menu.toggle(true);
			break;
		case R.id.help:
			if(menu.isSecondaryMenuShowing()) {
				menu.toggle(true);
			} else {
				menu.showSecondaryMenu(true);
			}
			break;
		}
		return false;
	}


	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	}


	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub

		Fragment f = null;
		Fragment glo = null;
		
		switch(arg2) {
		case 1:
			f = new TimeInState();
			glo = new CpuGlossaryFragment();
			break;
		case 2:
			f = new CPUInfo();
			glo = new CpuGlossaryFragment();
			break;
		case 4:
			f = new CpuPreferenceFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 5:
			f = new GpuPreferenceFragment();
			glo = new GpuGlossaryFragment();
			break;
		case 6:
			f = new UvPreferenceFragment();
			glo = new UvGlossaryFragment();
			break;
		case 8:
			f = new KernelPreferenceFragment();
			glo = new KernelGlossaryFragment();
			break;
		case 9:
			f = new LowMemoryKillerFragment();
			glo = new LmkGlossaryFragment();
			break;
		case 10:
			f = new VM();
			glo = new VmGlossaryFragment();
			break;
		case 12:
			f = new ReviewBootPreferenceFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 14:
			f = new FileManagerFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 15:
			f = new BackupFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 16:
			f = new CustomRecoveryCommandFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 18:
			f = new PropModder();
			glo = new CpuGlossaryFragment();
			break;
		case 19:
			f = new InitD();
			glo = new CpuGlossaryFragment();
			break;
		case 20:
			f = new WallpaperEffectsFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 22:
			f = new SettingsFragment();
			glo = new CpuGlossaryFragment();
			break;
		case 23:
			f = new infos();
			glo = new CpuGlossaryFragment();
			//showCredits();
			break;

		}
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		// This adds the newly created Preference fragment to my main layout, shown below
		ft.replace(R.id.activity_container,f);
		ft.replace(R.id.menu_frame, glo);
		// By hiding the main fragment, transparency isn't an issue
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		ft.addToBackStack("TAG");
		ft.commit();
		//menu.toggle(true);
	}

	@Override
	public void onBackPressed(){
		FragmentManager fm = getFragmentManager();
		if (fm.getBackStackEntryCount() > 0) {
			Log.i("MainActivity", "popping backstack");
			fm.popBackStack();

		} else {
			Log.i("MainActivity", "nothing on backstack, calling super");
			super.onBackPressed();
		}
	}


	public void showRootWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.rc_title));
		builder.setMessage(getResources().getString(R.string.rc_desc));
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				MainActivity.this.finish();
			}
		});
		builder.create().show();
	}

	public void showBusyBoxWarning() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.bb_title));
		builder.setMessage(getResources().getString(R.string.bb_desc));
		builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				MainActivity.this.finish();
			}
		});
		builder.create().show();
	}

	public void showCredits() {
		PackageInfo pInfo = null;
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = pInfo.versionName;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getString(R.string.app_name) + " V: "+version);
		builder.setIcon(R.drawable.ic_launcher);
		builder.setMessage(Html.fromHtml("<p>"+
				"Some <strong>KernelTweaker</strong> classes and xmls are based on OpenSource projects:</p>"+
				"<br><p><strong>AOKP</strong> :&nbsp;<a href=\"https://github.com/AOKP\">https://github.com/AOKP</a><br />"+
				"<strong>OMNI</strong>:&nbsp;<a href=\"https://github.com/omnirom/\">https://github.com/omnirom/</a><br />"+
				"<strong>Root-Tools&nbsp;</strong>:&nbsp;<a href=\"https://code.google.com/p/roottools/\">https://code.google.com/p/roottools/</a><br />"+
				"<strong>SlidingMenu by jfeinstein10&nbsp;</strong>:&nbsp;<a href=\"https://github.com/jfeinstein10/SlidingMenu\">https://github.com/jfeinstein10/SlidingMenu</a></p>"+
				"<p>"+
				"<u>A HUGE thanks goes to these guys, who makes Android better every day. A lot of thanks also to all our kernel developers, without their work this application has no reason to exist!</u></p>"));
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.create().show();

	}

	private void mountPartitions() {
		CommandCapture command = new CommandCapture(0, "busybox mount -o rw,remount /sys");
		try {
			RootTools.getShell(true).add(command);
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

	public static void setAppTheme() {
		boolean light = mPrefs.getBoolean(SettingsFragment.KEY_THEME, false);
		if(light) {
			mContext.setTheme(R.style.AppLight);
			menu.setBackground(mContext.getResources().getDrawable(R.drawable.bg_menu_light));
			mContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_light));
			mGlossaryContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_menu_light));
			menulist.setDivider(new ColorDrawable(Color.parseColor("#bbbbbb")));
			menulist.setDividerHeight(2);
		}else {
			mContext.setTheme(R.style.AppTheme);
			menu.setBackground(mContext.getResources().getDrawable(R.drawable.bg_menu_dark));;
			mContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_dark));
			mGlossaryContainer.setBackground(mContext.getResources().getDrawable(R.drawable.bg_menu_dark));
		}

	}


	private void copyHelpers() {

		if(!new File(this.getFilesDir().getPath()+"/helpers.sh").exists()) {

			InputStream stream = null;
			OutputStream output = null;

			try {
				stream = this.getAssets().open("helpers.sh");
				output = new BufferedOutputStream(new FileOutputStream(this.getFilesDir() + "/helpers.sh"));

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
