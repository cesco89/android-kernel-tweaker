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
import android.preference.CheckBoxPreference;
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

public class CustomCheckBoxPreference extends CheckBoxPreference implements OnCheckedChangeListener {

	String color = "#FFFFFF";
	TextView title;
	TextView summary;
	CheckBox checkbox;
	CheckBox cb;
	View separator;
	CustomCheckBoxPreference pref;
	DatabaseHandler db = MainActivity.db;
	List<DataItem> items = db.getAllItems();
	String category;
	String value;
	boolean bootEnabled = true;
	boolean hide = false;
	boolean checked = false;
	SharedPreferences mPrefs;

	public CustomCheckBoxPreference(Context context) {
		super(context);
		setLayoutResource(R.layout.preference);
	}

	public CustomCheckBoxPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference);
	}

	public CustomCheckBoxPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setLayoutResource(R.layout.preference);
	}

	public void setTitleColor(String color) {
		this.color = color;
	}

	public void setCategory(String cat) {
		this.category = cat;
	}

	public String getCategory() {
		return this.category;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

	public void setBootEnabled(boolean enabled) {
		this.bootEnabled = enabled;
	}

	public boolean isBootEnabled() {
		return this.bootEnabled;
	}
	
	public void setBootChecked(boolean checked) {
		this.checked = checked;
	}
	
	public void hideBoot(boolean hide) {
		this.hide = hide;
	}
	
	public boolean isBootChecked() {
		return this.checked;
	}


	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
		title = (TextView) view.findViewById(android.R.id.title);
		title.setTextColor(Color.parseColor(color));
		summary = (TextView) view.findViewById(android.R.id.summary);
		checkbox = (CheckBox) view.findViewById(android.R.id.checkbox);
		pref = this;
		
		title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		view.setBackground(getContext().getResources().getDrawable(R.drawable.selector));
		
		cb = (CheckBox) view.findViewById(R.id.cb);
		cb.setChecked(mPrefs.getBoolean(this.getTitle().toString(), false));
		cb.setOnCheckedChangeListener(this);
		
		separator = (View) view.findViewById(R.id.separator);
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
		updateDb(pref,pref.getValue().toString(), checked);
		this.checked = checked;
		SharedPreferences.Editor editor = mPrefs.edit();
		editor.putBoolean(pref.getTitle().toString(), checked);
		editor.commit();
	}
	
	private void hideBootViews(boolean hide) {
		if(hide) {
			separator.setVisibility(View.GONE);
			cb.setVisibility(View.GONE);
		} 
	}

}
