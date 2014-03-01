/*
 * Performance Control - An Android CPU Control application Copyright (C)
 * Brandon Valosek, 2011 <bvalosek@gmail.com> Copyright (C) Modified by 2012
 * <James Roberts "xoomdevnet@gmail.com">
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

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dsht.kerneltweaker.Helpers;
import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;
import com.dsht.open.CPUStateMonitor.CPUStateMonitorException;
import com.dsht.open.CPUStateMonitor.CpuState;

public class TimeInState extends Fragment implements Constants {

	private LinearLayout mStatesView;
	private TextView mAdditionalStates;
	private TextView mTotalStateTime;
	private TextView mHeaderAdditionalStates;
	private TextView mHeaderTotalStateTime;
	private TextView mStatesWarning;
	private boolean mUpdatingData = false;
	private LayoutInflater mInflater;
	private int mCpuNum = 1;
	private CpuInfoListAdapter mCpuInfoListAdapter;
	private CurCPUThread mCurCPUThread;

	private CPUStateMonitor monitor = new CPUStateMonitor();
	private Context context;
	private SharedPreferences preferences;
	private List<String> mCpuInfoListData;

	public class CpuInfoListAdapter extends ArrayAdapter<String> {

		public CpuInfoListAdapter(Context context, int resource, List<String> values) {
			super(context, R.layout.cpu_info_item, resource, values);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = mInflater.inflate(R.layout.cpu_info_item, parent, false);
			TextView cpuInfoCore = (TextView) rowView.findViewById(R.id.cpu_info_core);
			TextView cpuInfoFreq = (TextView) rowView.findViewById(R.id.cpu_info_freq);
			cpuInfoCore.setText(getString(R.string.core) + " " + String.valueOf(position) + ": ");
			cpuInfoFreq.setText(mCpuInfoListData.get(position));
			return rowView;
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getActivity();
		preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (savedInstanceState != null) {
			mUpdatingData = savedInstanceState.getBoolean("updatingData");
		}
		loadOffsets();
		mCpuNum = Helpers.getNumOfCpus();
		setRetainInstance(true);
		setHasOptionsMenu(true);
		if(MainActivity.menu != null && MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
		super.onCreateView(inflater, root, savedInstanceState);

		View view = inflater.inflate(R.layout.time_in_state, root, false);

		mStatesView = (LinearLayout) view.findViewById(R.id.ui_states_view);
		mAdditionalStates = (TextView) view.findViewById(R.id.ui_additional_states);
		mHeaderAdditionalStates = (TextView) view.findViewById(R.id.ui_header_additional_states);
		mHeaderTotalStateTime = (TextView) view.findViewById(R.id.ui_header_total_state_time);
		mStatesWarning = (TextView) view.findViewById(R.id.ui_states_warning);
		mTotalStateTime = (TextView) view.findViewById(R.id.ui_total_state_time);
		mInflater = inflater;
		mCpuInfoListData = new ArrayList<String>(mCpuNum);
		for (int i = 0; i < mCpuNum; i++) {
			mCpuInfoListData.add("Core " + String.valueOf(i) + ": ");
		}

		mCpuInfoListAdapter = new CpuInfoListAdapter(
				context, android.R.layout.simple_list_item_1, mCpuInfoListData);

		ListView mCpuInfoList = (ListView) view.findViewById(R.id.cpu_info_list);
		mCpuInfoList.setAdapter(mCpuInfoListAdapter);
		mCurCPUThread = new CurCPUThread();
		mCurCPUThread.start();
		return view;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("updatingData", mUpdatingData);
	}

	@Override
	public void onResume() {
		refreshData();
		if (mCurCPUThread == null) {
			mCurCPUThread = new CurCPUThread();
			mCurCPUThread.start();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (mCurCPUThread != null) {
			if (mCurCPUThread.isAlive()) {
				mCurCPUThread.interrupt();
				try {
					mCurCPUThread.join();
				} catch (InterruptedException e) {
				}
			}

			mCurCPUThread = null;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (!getResources().getBoolean(R.bool.config_showPerformanceOnly)) {
			inflater.inflate(R.menu.time_in_state_menu, menu);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.refresh:
			refreshData();
			break;
		case R.id.reset:
			try {
				monitor.setOffsets();
			} catch (Exception e) {
				// not good
			}

			saveOffsets();
			updateView();
			break;
		case R.id.restore:
			monitor.removeOffsets();
			saveOffsets();
			updateView();
			break;
		}

		return true;
	}

	public void updateView() {
		mStatesView.removeAllViews();
		List<String> extraStates = new ArrayList<String>();
		for (CpuState state : monitor.getStates()) {
			if (state.duration > 0) {
				generateStateRow(state, mStatesView);
			} else {
				if (state.freq == 0) {
					extraStates.add(getString(R.string.deep_sleep));
				} else {
					extraStates.add(state.freq / 1000 + " MHz");
				}
			}
		}

		if (monitor.getStates().size() == 0) {
			mStatesWarning.setVisibility(View.VISIBLE);
			mHeaderTotalStateTime.setVisibility(View.GONE);
			mTotalStateTime.setVisibility(View.GONE);
			mStatesView.setVisibility(View.GONE);
		}

		long totTime = monitor.getTotalStateTime() / 100;
		mTotalStateTime.setText(toString(totTime));

		if (extraStates.size() > 0) {
			int n = 0;
			String str = "";

			for (String s : extraStates) {
				if (n++ > 0)
					str += ", ";
				str += s;
			}

			mAdditionalStates.setVisibility(View.VISIBLE);
			mHeaderAdditionalStates.setVisibility(View.VISIBLE);
			mAdditionalStates.setText(str);
		} else {
			mAdditionalStates.setVisibility(View.GONE);
			mHeaderAdditionalStates.setVisibility(View.GONE);
		}
	}

	public void refreshData() {
		if (!mUpdatingData) {
			new RefreshStateDataTask().execute((Void) null);
		}
	}

	private static String toString(long tSec) {
		long h = (long) Math.floor(tSec / (60 * 60));
		long m = (long) Math.floor((tSec - h * 60 * 60) / 60);
		long s = tSec % 60;
		String sDur;
		sDur = h + ":";
		if (m < 10)
			sDur += "0";
		sDur += m + ":";
		if (s < 10)
			sDur += "0";
		sDur += s;

		return sDur;
	}

	private View generateStateRow(CpuState state, ViewGroup parent) {

		LayoutInflater inflater = LayoutInflater.from(context);
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.state_row, parent, false);

		float per = (float) state.duration * 100 / monitor.getTotalStateTime();
		String sPer = (int) per + "%";


		String sFreq;
		if (state.freq == 0) {
			sFreq = getString(R.string.deep_sleep);
		} else {
			sFreq = state.freq / 1000 + " MHz";
		}

		long tSec = state.duration / 100;
		String sDur = toString(tSec);

		TextView freqText = (TextView) view.findViewById(R.id.ui_freq_text);
		TextView durText = (TextView) view.findViewById(R.id.ui_duration_text);
		TextView perText = (TextView) view.findViewById(R.id.ui_percentage_text);
		ProgressBar bar = (ProgressBar) view.findViewById(R.id.ui_bar);

		freqText.setText(sFreq);
		perText.setText(sPer);
		durText.setText(sDur);
		bar.setProgress((int) per);

		parent.addView(view);
		return view;
	}

	protected class RefreshStateDataTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... v) {
			try {
				monitor.updateStates();
			} catch (CPUStateMonitorException e) {
			}
			return null;
		}

		@Override
		protected void onPreExecute() {
			mUpdatingData = true;
		}

		@Override
		protected void onPostExecute(Void v) {
			updateView();
			mUpdatingData = false;
		}
	}

	public void loadOffsets() {
		String prefs = preferences.getString(PREF_OFFSETS, "");
		if (prefs == null || prefs.length() < 1) {
			return;
		}

		Map<Integer, Long> offsets = new HashMap<Integer, Long>();
		String[] sOffsets = prefs.split(",");
		for (String offset : sOffsets) {
			String[] parts = offset.split(" ");
			offsets.put(Integer.parseInt(parts[0]), Long.parseLong(parts[1]));
		}

		monitor.setOffsets(offsets);
	}

	public void saveOffsets() {
		SharedPreferences.Editor editor = preferences.edit();
		String str = "";
		for (Map.Entry<Integer, Long> entry : monitor.getOffsets().entrySet()) {
			str += entry.getKey() + " " + entry.getValue() + ",";
		}
		editor.putString(PREF_OFFSETS, str).commit();
	}


	protected class CurCPUThread extends Thread {
		private boolean mInterrupt = false;

		public void interrupt() {
			mInterrupt = true;
		}

		@Override
		public void run() {
			try {
				while (!mInterrupt) {
					sleep(500);
					List<String> freqs = new ArrayList<String>();
					for (int i = 0; i < mCpuNum; i++) {
						String cpuFreq = CPU_PATH + String.valueOf(i) + CPU_FREQ_TAIL;
						String curFreq = "0";
						if (Helpers.fileExists(cpuFreq)) {
							curFreq = Helpers.readOneLine(cpuFreq);
						}
						freqs.add(curFreq);
					}
					String[] freqArray = freqs.toArray(new String[freqs.size()]);
					mCurCPUHandler.sendMessage(mCurCPUHandler.obtainMessage(0, freqArray));
				}
			} catch (InterruptedException e) {
				//return;
			}
		}
	}

	protected Handler mCurCPUHandler = new Handler() {
		public void handleMessage(Message msg) {
			String[] freqArray = (String[]) msg.obj;
			for (int i = 0; i < freqArray.length; i++) {
				// Convert freq in MHz
				try {
					int freqHz = Integer.parseInt(freqArray[i]);

					if (freqHz == 0) {
						mCpuInfoListData.set(i, getString(R.string.core_offline));
					} else {
						mCpuInfoListData.set(i, Integer.toString(freqHz / 1000) + " MHz");
					}
				} catch (Exception e) {
					// Do nothing
				}
			}
			mCpuInfoListAdapter.notifyDataSetChanged();
		}
	};
}
