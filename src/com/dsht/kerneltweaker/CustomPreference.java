package com.dsht.kerneltweaker;

import java.util.List;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class CustomPreference extends Preference implements OnCheckedChangeListener {

	TextView title;
	TextView summary;
	CheckBox cb;
	View separator;
	String color = "#FFFFFF";
	String sumColor = null;
	DatabaseHandler db = MainActivity.db;
	Preference pref;
	List<DataItem> items = db.getAllItems();
	boolean excludeDialog;
	boolean checkBoxState;
	boolean areMilliVolts;
	boolean hide = false;
	boolean checked = false;
	String category;
	Context mContext;
	int ID = 0;
	SharedPreferences mPrefs;

	public CustomPreference(Context context, boolean excludeDialog, String category) {
		super(context);
		this.mContext = context;
		this.excludeDialog = excludeDialog;
		this.category = category;
		setLayoutResource(R.layout.preference);
	}

	public CustomPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference);
	}

	public CustomPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.preference);
	}

	public void setCustomSummaryKeyPlus(int plus) {
		String currValue = this.getKey();
		int newValue = Integer.parseInt(currValue) + plus;
		this.setKey(newValue+"");
		if(areMilliVolts) {
			this.setSummary(newValue + " mV");
		} else {
			this.setSummary(newValue+"");
		}
	}

	public void setCustomSummaryKeyMinus(int minus) {
		String currValue = this.getKey();
		int newValue = Integer.parseInt(currValue) - minus;
		this.setKey(newValue+"");
		if(areMilliVolts) {
			this.setSummary(newValue + " mV");
		} else {
			this.setSummary(newValue+"");
		}
	}

	public void restoreSummaryKey(String summary, String key) {
		this.setKey(key);
		if(areMilliVolts) {
			this.setSummary(summary + " mV");
		} else {
			this.setSummary(summary+"");
		}
	}

	public void areMilliVolts(boolean areMillivolts) {
		this.areMilliVolts = areMillivolts;
	}

	public boolean getCheckBoxState() {
		return checkBoxState;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setTitleColor(String color) {
		this.color = color;
	}

	public void setSummaryColor(String color) {
		this.sumColor = color;
	}

	public void excludeFromDialog(boolean exclude) {
		this.excludeDialog = exclude;
	}

	public void setID(int id) {
		this.ID = id;
	}

	public int getID() {
		return this.ID;
	}

	public String getCategory() {
		return this.category;
	}

	public void setBootChecked(boolean checked) {
		this.checked = checked;
	}
	
	public boolean isBootChecked() {
		return this.checked;
	}
	
	public void hideBoot(boolean hide) {
		this.hide = hide;
	}
	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		pref = this;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
		title = (TextView) view.findViewById(android.R.id.title);
		title.setTextColor(Color.parseColor(color));
		summary = (TextView) view.findViewById(android.R.id.summary);
		if(sumColor != null) {
			summary.setTextColor(Color.parseColor(sumColor));
		}

		title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		view.setBackground(getContext().getResources().getDrawable(R.drawable.selector));
		
		cb = (CheckBox) view.findViewById(R.id.cb);
		cb.setChecked(mPrefs.getBoolean(this.getTitle().toString(), false));
		cb.setOnCheckedChangeListener(this);
		
		separator = (View) view.findViewById(R.id.separator);
		
		hideBootViews(hide);
		checked = mPrefs.getBoolean(this.getTitle().toString(), false);
	}


	public void updateDb( final Preference p, final String value,final boolean isChecked) {

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
					db.addItem(new DataItem("'"+p.getKey()+"'", value, p.getTitle().toString(), category));
				} else {
					if(db.getContactsCount() != 0) {
						db.deleteItemByName("'"+p.getKey()+"'");
					}
				}

				return "Executed";
			}
			@Override
			protected void onPostExecute(String result) {
				items = db.getAllItems();
			}
		}
		new LongOperation().execute();
	}

	@Override
	public void onCheckedChanged(CompoundButton arg0, boolean checked) {
		// TODO Auto-generated method stub
		updateDb(pref,pref.getSummary().toString(), checked);
		this.checked = checked;
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(pref.getTitle().toString(), checked);
		editor.commit();
	}
	
	private void hideBootViews(boolean hide) {
		if(hide) {
			separator.setVisibility(View.GONE);
			cb.setVisibility(View.GONE);
		} else {
			separator.setVisibility(View.VISIBLE);
			cb.setVisibility(View.VISIBLE);
		}
	}
}
