package com.dsht.settings;

import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.wizard.WizardActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;

public class infos extends PreferenceFragment implements OnPreferenceClickListener {

	private String KEY_DSHT = "key_dsht";
	private String KEY_CESCO = "key_cesco";
	private String KEY_SOLLYX = "key_sollyx";
	private String KEY_AOKP = "key_aokp";
	private String KEY_OMNI = "key_omni";
	private String KEY_DU = "key_du";
	private String KEY_SLIDINGMENU = "key_slidingmenu";

	private Preference mDsht, 
	mCesco, 
	mSollyx,
	mAokp,
	mOmni,
	mDu,
	mSlidingMenu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.infos);

		mDsht = (Preference) findPreference(KEY_DSHT);
		mCesco = (Preference) findPreference(KEY_CESCO);
		mSollyx = (Preference) findPreference(KEY_SOLLYX);
		mAokp = (Preference) findPreference(KEY_AOKP);
		mOmni = (Preference) findPreference(KEY_OMNI);
		mDu = (Preference) findPreference(KEY_DU);
		mSlidingMenu = (Preference) findPreference(KEY_SLIDINGMENU);

		PackageInfo pInfo = null;
		try {
			pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String version = pInfo.versionName;

		mDsht.setTitle(R.string.app_name);
		mDsht.setIcon(R.drawable.ic_launcher);
		mDsht.setSummary("Version: "+version);

		mCesco.setIcon(R.drawable.cesco);
		mSollyx.setIcon(R.drawable.sollyx_google);

		/*	
	    mAokp.setIcon(R.drawable.aokp);
		mOmni.setIcon(R.drawable.omni);
		mDu.setIcon(R.drawable.du); 
		*/

		mSlidingMenu.setIcon(R.drawable.github);

		mDsht.setOnPreferenceClickListener(this);
		mCesco.setOnPreferenceClickListener(this);
		mSollyx.setOnPreferenceClickListener(this);
		//mAokp.setOnPreferenceClickListener(this);
		//mOmni.setOnPreferenceClickListener(this);
		//mDu.setOnPreferenceClickListener(this);
		mSlidingMenu.setOnPreferenceClickListener(this);

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
	}
/*
 *
 * WIP: First start wizard
 *
 *
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.layout_list, container,false);
		ListView listView = (ListView) v.findViewById(android.R.id.list);
		View header = inflater.inflate(R.layout.header, null, false);
		listView.addHeaderView(header);
		ImageView logo = (ImageView) header.findViewById(R.id.imageView1);
		logo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				getActivity().startActivity(new Intent(getActivity(), WizardActivity.class));
			}
			
		});
		return v;
	}
 */

	@Override
	public boolean onPreferenceClick(Preference pref) {
		// TODO Auto-generated method stub
		String url = "";
		if(pref == mDsht) {
			url = "https://play.google.com/store/apps/developer?id=DSHT";
		}
		if(pref == mCesco) {
			url = "https://plus.google.com/u/0/+FrancescoRigamonti/posts";
		}
		if(pref == mSollyx) {
			url = "https://plus.google.com/u/0/116757450567339042397/posts";
		}
		/*
		if(pref == mAokp) {
			url = "https://github.com/AOKP";
		}
		if(pref == mOmni) {
			url = "https://github.com/omnirom/";
		}
		if(pref == mDu) {
			url = "https://github.com/DirtyUnicorns-KitKat/";
		}
		*/
		if(pref == mSlidingMenu) {
			url = "https://github.com/jfeinstein10/slidingmenu";
		}
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);

		return false;
	}



}
