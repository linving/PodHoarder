package com.podhoarderproject.podhoarder.adapter;

import com.podhoarderproject.podhoarder.gui.FeedDetailsFragment;
import com.podhoarderproject.podhoarder.gui.FeedFragment;
import com.podhoarderproject.podhoarder.gui.LatestEpisodesFragment;
import com.podhoarderproject.podhoarder.gui.PlayerFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

public class TabFragmentsAdapter extends FragmentStatePagerAdapter 
{
    private final FragmentManager mFragmentManager;
    public Fragment mFragmentAtPos0;
    FirstPageListener listener = new FirstPageListener();

    public TabFragmentsAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
        mFragmentManager = fragmentManager;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) 
        {
	        case 0: // Fragment # 0
	            if (mFragmentAtPos0 == null)
	            {
	                mFragmentAtPos0 = new FeedFragment(listener);
	            }
	            return mFragmentAtPos0;
	
	        case 1: // Fragment # 1
	            return new LatestEpisodesFragment();
	        case 2:// Fragment # 2
	            return new PlayerFragment();
        }
        return null;
    }

    @Override
    public int getCount() 
    {
        return 3;
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (object instanceof FeedFragment && mFragmentAtPos0 instanceof FeedDetailsFragment) {
            return POSITION_NONE;
        }
        if (object instanceof FeedDetailsFragment && mFragmentAtPos0 instanceof FeedFragment) {
            return POSITION_NONE;
        }
        return POSITION_UNCHANGED;
    }

    
    private final class FirstPageListener implements FirstPageFragmentListener 
    {
        public void onSwitchToNextFragment() 
        {
            mFragmentManager.beginTransaction().setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE).remove(mFragmentAtPos0).commit();
            if (mFragmentAtPos0 instanceof FeedFragment)
            {
                mFragmentAtPos0 = new FeedDetailsFragment(listener);
            }
            else
            { // Instance of NextFragment
                mFragmentAtPos0 = new FeedFragment(listener);
            }
            notifyDataSetChanged();
        }
    }
}

