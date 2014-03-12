package com.dsht.wizard;

import com.dsht.jazzy.JazzyViewPager;
import com.dsht.jazzy.JazzyViewPager.TransitionEffect;
import com.dsht.kerneltweaker.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ViewGroup;

public class WizardActivity extends FragmentActivity {
	
	private JazzyViewPager mPager;
	private myPagerAdapter mPagerAdapter;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard_layout);
		
		mPager = (JazzyViewPager)findViewById(R.id.pager);
		mPager.setTransitionEffect(TransitionEffect.Tablet);
		mPager.setFadeEnabled(true);
        
        mPagerAdapter = new myPagerAdapter(this.getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
	}
	
	private class myPagerAdapter extends FragmentPagerAdapter {

		public myPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
		    Object obj = super.instantiateItem(container, position);
		    mPager.setObjectForPosition(obj, position);
		    return obj;
		}

		@Override
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			Fragment f = new Fragment();
			switch(arg0) {
			case 0:
				f = new DummyFragment();
				break;
			case 1:
				f = new DummyFragment();
				break;
			case 2:
				f = new DummyFragment();
				break;
			case 3:
				f = new DummyFragment();
				break;
			case 4:
				f = new DummyFragment();
				break;
			case 5:
				f = new DummyFragment();
				break;
			case 6:
				f = new DummyFragment();
				break;
			case 7:
				f = new DummyFragment();
				break;
			case 8:
				f = new DummyFragment();
				break;
			case 9:
				f = new DummyFragment();
				break;
			case 10:
				f = new DummyFragment();
				break;
			case 11:
				f = new DummyFragment();
				break;
			case 12:
				f = new DummyFragment();
				break;
			case 13:
				f = new DummyFragment();
				break;
			case 14:
				f = new DummyFragment();
				break;
			}
			return f;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 15;
		}
		
	}

}
