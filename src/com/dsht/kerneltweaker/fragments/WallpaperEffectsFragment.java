package com.dsht.kerneltweaker.fragments;

import java.io.IOException;

import com.dsht.kerneltweaker.MainActivity;
import com.dsht.kerneltweaker.R;

import android.app.Fragment;
import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v8.renderscript.*;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class WallpaperEffectsFragment extends Fragment implements OnSeekBarChangeListener, OnClickListener {

	private ImageView mWall;
	private SeekBar mBlur;
	private Button mApply;
	private Bitmap source;
	private WallpaperManager wallpaperManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if(MainActivity.menu.isMenuShowing()) {
			MainActivity.menu.toggle(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View v = inflater.inflate(R.layout.wallpaper_effects, container,false);

		mWall = (ImageView) v.findViewById(R.id.wall);
		mBlur = (SeekBar) v.findViewById(R.id.sb_blur);
		mApply = (Button) v.findViewById(R.id.btn_apply);
		mWall.setDrawingCacheEnabled(true);
		mBlur.setMax(25);

		mBlur.setOnSeekBarChangeListener(this);
		mApply.setOnClickListener(this);
		
		wallpaperManager = WallpaperManager.getInstance(getActivity());
		final Drawable wallpaperDrawable = wallpaperManager.getDrawable();

		mWall.setImageDrawable(wallpaperDrawable);

		source = drawableToBitmap(wallpaperDrawable);

		return v;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()) {
		case R.id.btn_apply:
			 try {
                 wallpaperManager.setBitmap(mWall.getDrawingCache());
             } catch (IOException e) {
                 e.printStackTrace();
             }
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		int rad = arg0.getProgress();
		if(rad == 0) {
			mWall.setImageBitmap(source);
		}else {
			Bitmap blurred = BlurImage(source, rad);
			mWall.setImageBitmap(blurred);	
		}

	}

	public static Bitmap drawableToBitmap (Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable)drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap); 
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}


	private Bitmap BlurImage (Bitmap input, int radius)
	{
		RenderScript rsScript = RenderScript.create(getActivity());
		Allocation alloc = Allocation.createFromBitmap(rsScript, input);

		ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript,Element.U8_4(rsScript));
		blur.setRadius (radius);
		blur.setInput (alloc);

		Bitmap result = Bitmap.createBitmap(input.getWidth(), input.getHeight(), input.getConfig());
		Allocation outAlloc = Allocation.createFromBitmap (rsScript, result);
		blur.forEach (outAlloc);
		outAlloc.copyTo(result);

		rsScript.destroy();
		return result;
	}

}
