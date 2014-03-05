package com.dsht.kerneltweaker;

import java.util.List;

import com.dsht.kerneltweaker.database.DataItem;
import com.dsht.kerneltweaker.database.DatabaseHandler;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomListPreference extends ListPreference implements OnCheckedChangeListener {

	String color = "#FFFFFF";
	TextView title;
	TextView summary;
	ImageView icon;
	CheckBox cb;
	View separator;
	Context mContext;
	DatabaseHandler db = MainActivity.db;
	CustomListPreference pref;
	List<DataItem> items = db.getAllItems();
	Drawable mIcon = null;
	View mView;
	int mIconResId;
	String mCurrentValue;
	boolean checkboxState;
	String category;
	boolean exclude = false;
	boolean hide = false;
	boolean ischecked = false;
	SharedPreferences mPrefs;

	public CustomListPreference(Context context, String category) {
		super(context);
		this.category = category;
		setLayoutResource(R.layout.preference);
	}

	public CustomListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setLayoutResource(R.layout.preference);
	}

	public void setTitleColor(String color) {
		this.color = color;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void excludeFromBoot(boolean exclude) {
		this.exclude = exclude;
	}

	public String getCategory() {
		return this.category;
	}

	public void setBootChecked(boolean checked) {
		ischecked = checked;
	}

	public void hideBoot(boolean hide) {
		this.hide = hide;
	}
	
	public boolean isBootChecked() {
		return this.ischecked;
	}


	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		pref = this;
		mView = view;
		mPrefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.mContext);
		title = (TextView) view.findViewById(android.R.id.title);
		title.setTextColor(Color.parseColor(color));
		summary = (TextView) view.findViewById(android.R.id.summary);
		icon = (ImageView) view.findViewById(android.R.id.icon);
		cb = (CheckBox) view.findViewById(R.id.cb);
		
		cb.setChecked(mPrefs.getBoolean(this.getTitle().toString(), false));

		title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		view.setBackground(getContext().getResources().getDrawable(R.drawable.selector));
		
		cb.setOnCheckedChangeListener(this);
		separator = (View) view.findViewById(R.id.separator);
		hideBootViews(hide);
		ischecked = mPrefs.getBoolean(this.getTitle().toString(), false);
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
		this.ischecked = checked;
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
