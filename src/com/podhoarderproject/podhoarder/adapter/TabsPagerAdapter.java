package com.podhoarderproject.podhoarder.adapter;

import com.podhoarderproject.podhoarder.gui.*;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class TabsPagerAdapter extends FragmentStatePagerAdapter {

    public TabsPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int index) {

        switch (index) {
        case 0:
            // Top Rated fragment activity
        	return new FeedFragment();
        case 1:
            // Games fragment activity
            return new LatestEpisodesFragment();
	    case 2:
	        // Games fragment activity
	        return new PlayerFragment();
	    }
        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 3;
    }

}
