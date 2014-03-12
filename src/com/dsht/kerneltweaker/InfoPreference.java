package com.dsht.kerneltweaker;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class InfoPreference extends Preference implements OnClickListener {

	TextView title;
	TextView summary;
	Button mButton1;
	Button mButton2;
	Button mButton3;
	ImageView image;
	Context mContext;
	AttributeSet mAttrs;
	String b1text;
	String b2text;
	String b3text;
	String b1link, b2link, b3link;
	Drawable imageResource;
	View mView1;
	View mView2;

	public InfoPreference(Context context) {
		super(context);
		this.mContext = context;
		setLayoutResource(R.layout.info_preference);
	}

	public InfoPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mAttrs = attrs;
		this.mContext = context;
		setLayoutResource(R.layout.info_preference);
		init(attrs);
	}

	public InfoPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mAttrs = attrs;
		this.mContext = context;
		setLayoutResource(R.layout.info_preference);
		init(attrs);
	}

	
	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		title = (TextView) view.findViewById(android.R.id.title);
		summary = (TextView) view.findViewById(android.R.id.summary);
		mButton1=(Button)view.findViewById(R.id.button1);
		mButton2=(Button)view.findViewById(R.id.button2);
		mButton3=(Button)view.findViewById(R.id.button3);
		image=(ImageView)view.findViewById(R.id.image);
		mView1=(View)view.findViewById(R.id.view1);
		mView2=(View)view.findViewById(R.id.view2);
		
		title.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		summary.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
		mButton1.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		mButton2.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		mButton3.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
		
		mButton1.setText(b1text);
		mButton2.setText(b2text);
		mButton3.setText(b3text);
		
		image.setImageDrawable(imageResource);
		
		mButton1.setOnClickListener(this);
		mButton2.setOnClickListener(this);
		mButton3.setOnClickListener(this);
		
		if(b1link == null) {
			mButton1.setVisibility(View.GONE);
			mView1.setVisibility(View.GONE);
			
		}
		if(b2link == null) {
			mButton2.setVisibility(View.GONE);
			mView2.setVisibility(View.GONE);
		}
		if(b3link == null) {
			mButton3.setVisibility(View.GONE);
			mView2.setVisibility(View.GONE);
		}
		
	}

	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		String link = null;
		switch(arg0.getId()) {
		case R.id.button1:
			link = b1link;
			break;
		case R.id.button2:
			link = b2link;
			break;
		case R.id.button3:
			link = b3link;
			break;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		mContext.startActivity(intent);
		
	}
	
	private void init(AttributeSet attrs) {
		if (attrs != null) {
			final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.InfoPreference);
			b1text = a.getString(R.styleable.InfoPreference_button1_text);
			b2text = a.getString(R.styleable.InfoPreference_button2_text);
			b3text = a.getString(R.styleable.InfoPreference_button3_text);
			
			b1link = a.getString(R.styleable.InfoPreference_button1_link);
			b2link = a.getString(R.styleable.InfoPreference_button2_link);
			b3link = a.getString(R.styleable.InfoPreference_button3_link);
			
			imageResource = a.getDrawable(R.styleable.InfoPreference_image);
			
			a.recycle();
		}
	}

}
