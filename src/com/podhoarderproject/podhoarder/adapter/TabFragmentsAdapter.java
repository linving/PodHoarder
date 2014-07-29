package com.podhoarderproject.podhoarder.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.podhoarderproject.podhoarder.fragment.FeedDetailsFragment;
import com.podhoarderproject.podhoarder.fragment.FeedFragment;
import com.podhoarderproject.podhoarder.fragment.LatestEpisodesFragment;
import com.podhoarderproject.podhoarder.fragment.PlayerFragment;
import com.podhoarderproject.podhoarder.util.Constants;

public class TabFragmentsAdapter extends FragmentStatePagerAdapter 
{
	private boolean detailsPageEnabled = false;
	
    public TabFragmentsAdapter(FragmentManager fragmentManager) 
    {
        super(fragmentManager);
    }

    @Override
    public Fragment getItem(int position) 
    {
        switch (position) 
        {
	        case Constants.FEEDS_TAB_POSITION: 	// Fragment # 0
	            return new FeedFragment();
	        case Constants.LATEST_TAB_POSITION: // Fragment # 1
	            return new LatestEpisodesFragment();
	        case Constants.PLAYER_TAB_POSITION:	// Fragment # 2
	            return new PlayerFragment();
	        case Constants.FEED_DETAILS_TAB_POSITION:
	        	return new FeedDetailsFragment();
        }
        return null;
    }
    
    @Override
    public int getItemPosition(Object object)
    {
    	if(object instanceof FeedDetailsFragment) 
    	{ //this includes deleting or adding pages
    		 return FragmentStatePagerAdapter.POSITION_NONE;
    	}
    	else return FragmentStatePagerAdapter.POSITION_UNCHANGED; //this ensures high performance in other operations such as editing list items.

    }

    @Override
    public int getCount() 
    {
    	if (this.detailsPageEnabled)	return Constants.TAB_COUNT;
    	else return (Constants.TAB_COUNT - 1);
    }
    
    public void setDetailsPageEnabled(boolean enabled)
    {
    	this.detailsPageEnabled = enabled;
    	this.notifyDataSetChanged();
    }
}

