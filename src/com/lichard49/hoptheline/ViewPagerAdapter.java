package com.lichard49.hoptheline;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class ViewPagerAdapter extends FragmentPagerAdapter
{
	private Context context;
	 
    public ViewPagerAdapter(Context context, FragmentManager fm)
    {
        super(fm);
        this.context = context;
    }
    
    @Override
    public Fragment getItem(int position)
    {
        Fragment f = new Fragment();
        switch(position)
        {
        case 0:
            f = RequestsFragment.newInstance(context);
            break;
        case 1:
            f = OffersFragment.newInstance(context);
            break;
        }
        return f;
    }
    
    @Override
    public int getCount()
    {
        return 2;
    }
    
    @Override
    public CharSequence getPageTitle(int position)
    {
    	switch(position)
        {
        case 0:
            return "Requests";
        case 1:
            return "Offers";
        }
    	return null;
    }
}
