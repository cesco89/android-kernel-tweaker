/*
 * Performance Control - An Android CPU Control application Copyright (C) 2012
 * James Roberts
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.dsht.open;

import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;


public class PCSettings extends PreferenceFragment implements Constants, OnPreferenceChangeListener {

	SharedPreferences mPreferences;
	private CheckBoxPreference mLightThemePref;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
		addPreferencesFromResource(R.xml.pc_settings);

		mLightThemePref = (CheckBoxPreference) findPreference("use_light_theme");
		Preference mVersion = findPreference("version_info");
		mVersion.setTitle(getString(R.string.pt_ver) + VERSION_NUM);

	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
		String key = preference.getKey();
		if ("use_light_theme".equals(key)) {
			Helpers.restartPC(getActivity());
			return true;
		}
		return false;
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		return false;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

}
