package com.lichard49.hoptheline;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class HopLineActivity extends FragmentActivity
{
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
	    ViewPagerAdapter adapter = new ViewPagerAdapter(getApplicationContext(),getSupportFragmentManager());
	    viewPager.setAdapter(adapter);
	    viewPager.setCurrentItem(0);
	}
	
	@Override
	public void onPause() {
	    super.onPause();  // Always call the superclass method first
	    Log.d("onpause", "pause");
	    
	    Intent serviceIntent = new Intent(this, BackgroundService.class);
		startService(serviceIntent);
	}
	
	@Override
	public void onResume() {
	    super.onResume();  // Always call the superclass method first
	    Log.d("onResume", "resume");
	    
	    Intent serviceIntent = new Intent(this, BackgroundService.class);
		stopService(serviceIntent);
	}
}
